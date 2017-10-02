(ns mibot.comms.slack.component
  (:require
   [com.stuartsierra.component :as component]
   [clojure.core.async :refer [chan close!]]
   [http.async.client :as httpa]

   [mibot.comms.slack.core :as slack]
   ))

(defn run-comm [ahttp-client slack-config]
  (let [{:keys [api-token channel-name]} slack-config
        ws-url (slack/get-ws-url api-token)
        channel-id   (-> (slack/get-channel-map api-token)
                         (get channel-name))
        {{to-bot :from-ws from-bot :to-ws} :chans
         ;; contains (unused) reconnect information
         init-res :init-res
         } (slack/connect-socket channel-id ahttp-client ws-url)
        ]
    {:bot-chans [to-bot from-bot]}))

(defrecord Comm [slack-config input-chan]
  component/Lifecycle
  (start [component]
    (let [ahttp-client (httpa/create-client)
          {[to-bot from-bot] :bot-chans
           } (run-comm ahttp-client slack-config)]
      (-> component
          (assoc :ahttp-client ahttp-client)
          (assoc :from-bot from-bot)
          (assoc :to-bot to-bot)
          )))
  (stop [component]
    (when-let [to-bot (:to-bot component)]
      (close! to-bot))
    (when-let [ahttp-client (:ahttp-client component)]
      (-> ahttp-client .close))
    (-> component
        (assoc :to-bot nil)
        (assoc :from-bot nil)
        (assoc :ahttp-client nil)
        )))

;; no oauth, no muss
(defn new-comm [bot-api-token channel-name]
  {:pre? [(string? bot-api-token)]}
  (map->Comm {:slack-config {:api-token bot-api-token
                             :channel-name channel-name
                             }}))
