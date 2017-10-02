(ns mibot.comms.slack.core
  (:require
   [clojure.core.async :as async
    :refer [go go-loop chan close! <! >! <!!]]
   [clj-http.client :as http]
   [http.async.client :as httpa]
   [http.async.client.websocket :as ws]
   [cheshire.core :as cheshire]
   ))

;; how often to ping, provided there's no message for server
(def ^:dynamic *ping-timout* 3000)

(def rtm-socket-urls
  {:start "https://slack.com/api/rtm.start"
   :connect "https://slack.com/api/rtm.connect"})

(defonce msg-counter (atom 1))

(defn get-channel-map [api-token]
  "for channels joined by bot, maps names (strings) to ids (strings)"
  (let [res (-> "https://slack.com/api/channels.list"
                (http/get {:query-params {:token api-token
                                          :no_unreads true}
                           :as :json})
                (get :body))]
    (if (:ok res)
      (->> (:channels res)
           (filter :is_member)
           (map (juxt :name :id))
           (into {})
          )
      )))

(defn get-ws-url
  ([api-token] (get-ws-url api-token :connect))
  ([api-token url-key]
   (let [response
         (-> rtm-socket-urls
             (get url-key)
             (http/get {:query-params {:token api-token
                                       :no_unreads true}
                        :as :json})
             (get :body))]
     (if (:ok response)
       (:url response))))
  )

(defn get-socket-init-res [json-from-ws-chan & {do-sync :sync}]
  (let [init-res-chan (chan)]
    (go-loop []
      (let [ws-data (<! json-from-ws-chan)]
        (if (not (= "reconnect_url" (:type ws-data)))
          (recur)
          (>! init-res-chan {:reconnect-url (:url ws-data)}))
        ))
    (if do-sync
      (async/<!! init-res-chan)
      init-res-chan)))

(defn socket-text-handler [json-from-ws-chan _ text]
  (let [ws-data (cheshire/parse-string text true)]
    (if (contains? #{"message" "reconnect_url"}
                   (:type ws-data))
      (go (>! json-from-ws-chan ws-data))
      )))

(defn make-msg-from-ws-chan [channel-id json-from-ws-chan]
  (let [msg-from-ws-chan (chan)]
    (go-loop []
      (let [json-data (<! json-from-ws-chan)]
        (if json-data
          (if (and (= "message" (:type json-data))
                   (= channel-id (:channel json-data)))
            (do (>! msg-from-ws-chan (:text json-data))
                (recur))
            (recur))
          (close! json-from-ws-chan))
        ))
    msg-from-ws-chan))

(defn listen-for-msg-to-ws [msg-to-ws-chan channel-id
                            {:keys [ws-send-fn cleanup-fn] :as cbs}]
  "also send periodic pings to keep ws open if no msg"
  (go-loop [msg-count 1]
    (let [timeout-chan (async/timeout *ping-timout*)
          [msg-or-nil msg-or-timeout-chan
           ] (async/alts! [msg-to-ws-chan timeout-chan]
                          :priority true)]
      (if (= msg-or-timeout-chan msg-to-ws-chan)
        (if msg-or-nil
          (do (-> {:id msg-count
                   :type "message"
                   :channel channel-id
                   :text msg-or-nil
                   } cheshire/generate-string
                  ws-send-fn)
              (recur (inc msg-count)))
          (cleanup-fn))
        (do (-> {:id msg-count
                 :type "ping"
                 } cheshire/generate-string
                ws-send-fn)
            (recur (inc msg-count))))
      )))

(defn connect-socket [channel-id async-http-client url]
  "for cleanup, close `:chans` `:to-ws` producer-side"
  (let [json-from-ws-chan (chan)
        msg-to-ws-chan (chan)
        socket (httpa/websocket
                async-http-client url
                :close (fn []
                         ;; never called ?
                         (println "top of ws close cb")
                         )
                :error (fn []
                         ;; not called ?
                         (println "top of ws err cb")
                         (close! json-from-ws-chan)
                         )
                :text (partial socket-text-handler
                               json-from-ws-chan)
                )
        init-res (get-socket-init-res json-from-ws-chan :sync true)
        ]
    (listen-for-msg-to-ws
     msg-to-ws-chan channel-id
     {:ws-send-fn (fn [text]
                    (ws/send socket :text text))
      :cleanup-fn #(close! json-from-ws-chan)
      })
    {:chans {:from-ws (make-msg-from-ws-chan
                       channel-id json-from-ws-chan)
             :to-ws msg-to-ws-chan}
     :init-res init-res
     }))

(defn get-ws [async-http-client channel & args-get-url]
  (when-let [ws-url (apply get-ws-url args-get-url)]
    (connect-socket async-http-client ws-url)
    ))
