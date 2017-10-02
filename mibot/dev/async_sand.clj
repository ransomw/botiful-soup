(ns async-sand
  (:require
   [clojure.core.async :as async]
   ))

(defn alts-check []
  (let [timeout-chan (async/timeout 1000)
        other-chan (async/chan)]
    (async/go
      (let [[a-val a-chan
             ] (async/alts! [other-chan timeout-chan]
                            :priority true
                            )]
        (if (= a-chan timeout-chan)
          (println "got timeout chan")
          (println "got other chan")
          ))
      (let [[a-val a-chan
             ] (async/alts! [other-chan timeout-chan]
                            :priority true
                            )]
        (if (= a-chan timeout-chan)
          (println "got timeout chan")
          (println "got other chan")
          ))
      (async/close! other-chan)
      (let [[a-val a-chan
             ] (async/alts! [other-chan timeout-chan]
                            :priority true
                            )]
        (if (= a-chan timeout-chan)
          (println "got timeout chan")
          (println "got other chan")
          ))
      (let [[a-val a-chan
             ] (async/alts! [other-chan timeout-chan]
                            :priority true
                            )]
        (if (= a-chan timeout-chan)
          (println "got timeout chan")
          (println "got other chan")
          ))
      )
    nil))
