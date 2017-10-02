(ns mibot.bots.component
  (:require
   [clojure.set :as set]
   [clojure.core.async :refer [go-loop <! >! close!]]
   [com.stuartsierra.component :as component]
   ))

(defn run-bot [on-msg chans]
  (let [{input-chan :to-bot output-chan :from-bot} chans]
    (go-loop []
      (when-let [input (<! input-chan)]
        (when-let [output (on-msg input)]
          (>! output-chan output))
        (recur))
      )
    ))

;; on-msg: callback returns falsy or a string reply
;; comm: provides core.async chans for communication
(defrecord Bot [on-msg comm]
  component/Lifecycle
  (start [component]
    (run-bot on-msg
             (select-keys comm [:to-bot :from-bot]))
    (-> component
        (assoc :from-bot (:from-bot comm))
        ))
  (stop [component]
    (when-let [from-bot (:from-bot component)]
      (close! from-bot))
    (-> component
        (assoc :from-bot nil)
        )))

(defn new-bot [on-msg] (map->Bot {:on-msg on-msg}))
