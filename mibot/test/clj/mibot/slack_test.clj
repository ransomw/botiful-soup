(ns mibot.slack-test
  (:require
   [clojure.test :refer :all]
   [clojure.edn :as edn]
   [clojure.core.async :refer [close! <!! >!!]]
   [http.async.client :as httpa]

   [mibot.comms.slack.core :as slack]
   ))

;;; fixture

(def ^:dynamic *data-fixture*)
(def ^:dynamic *ahttp-client-one*)
(def ^:dynamic *ahttp-client-two*)

(defn read-data-fixture []
  (edn/read-string (slurp "test/clj/mibot/slack_test_fixture.edn")))

(defn slack-fixture [f]
  (with-open [client-one (httpa/create-client)
              client-two (httpa/create-client)]
    (binding [
              *data-fixture* (read-data-fixture)
              *ahttp-client-one* client-one
              *ahttp-client-two* client-two
              ]
      (f)
      )))

(use-fixtures :each slack-fixture)

;;; util

(defn get-channel-id [& {:keys [bot-token]}]
  (-> (slack/get-channel-map
       (or bot-token (:bot-token-one *data-fixture*)))
      (get (:test-channel *data-fixture*))))

;;; tests

(deftest get-channel-map
  (let [channel-map (slack/get-channel-map
                     (:bot-token-one *data-fixture*))
        channel-id (get-channel-id)]
    (is (map? channel-map))
    (is (string? (get channel-map (:test-channel *data-fixture*))))
    (is (string? channel-id))
    ))

(deftest login-test
  (let [ws-url (slack/get-ws-url (:bot-token-one *data-fixture*))
        {{:keys [reconnect-url]} :init-res
         {:keys [to-ws]} :chans
         } (slack/connect-socket (get-channel-id)
                                 *ahttp-client-one* ws-url)
        ]
    (is (not (nil? ws-url)))
    (is (string? ws-url))
    (is (string? reconnect-url))
    (close! to-ws)
    ))

(deftest send-recv-test
  (let [ws-url-one (slack/get-ws-url (:bot-token-one *data-fixture*))
        ws-url-two (slack/get-ws-url (:bot-token-two *data-fixture*))
        channel-id (get-channel-id)
        connect-socket (partial slack/connect-socket channel-id)
        {{from-ws :from-ws to-ws-one :to-ws} :chans
         } (connect-socket *ahttp-client-one* ws-url-one)
        {{to-ws :to-ws} :chans
         } (connect-socket *ahttp-client-two* ws-url-two)
        ]
    (is (= channel-id (get-channel-id
                       :bot-token (:bot-token-two *data-fixture*))))
    (>!! to-ws "testing...")
    (is (= "testing..." (<!! from-ws)))
    (close! to-ws)
    (close! to-ws-one)
    ))
