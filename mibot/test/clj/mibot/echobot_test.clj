(ns mibot.echobot-test
  ;; practically, this is a test of of the Bot component
  (:require
   [clojure.test :refer :all]
   [clojure.core.async :refer [go chan close! <!! >!!]]
   [com.stuartsierra.component :as component]

   [mibot.bots.core :refer [new-echo-bot]]
   ))

(def ^:dynamic *input-chan*)
(def ^:dynamic *output-chan*)

(defrecord MockComm [comm cmd-ns]
  component/Lifecycle
  (start [component]
    (-> component
        (assoc :to-bot (chan))
        (assoc :from-bot (chan))
        ))
  (stop [component]
    (when-let [to-bot (:to-bot component)]
      (close! to-bot))
    (-> component
        (assoc :to-bot nil)
        (assoc :from-bot nil)
        )))

(defn new-mock-comm [] (map->MockComm {}))

(defn echobot-test-system []
  (component/system-map
   :comm (new-mock-comm)
   :bot (component/using (new-echo-bot) [:comm])
  ))

(defn echobot-fixture [f]
  (let [system (component/start (echobot-test-system))
        comm (:comm system)
        bot (:bot system)]
    (binding [*input-chan* (:to-bot comm)
              *output-chan* (:from-bot comm)]
      (f)
      (component/stop system)
      )))

(use-fixtures :each echobot-fixture)

(deftest echo-test
  (>!! *input-chan* "echo")
  (is (= "echo" (<!! *output-chan*)))
  (>!! *input-chan* "asdf\nqwer")
  (is (= "asdf\nqwer" (<!! *output-chan*)))
  )

;; (deftest eval-test
;;   (>!! *input-chan* ",(+ 1 1)")
;;   ;; (is (= "2" (<!! *output-chan*)))
;;   (>!! *input-chan* ",(say-hello \"bob\")")
;;   ;; (is (= "hello, bob" (<!! *output-chan*)))
;;   )
