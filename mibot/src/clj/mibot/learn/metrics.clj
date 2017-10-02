(ns mibot.learn.metrics
  (:require
   [clojure.set :as set]
   ))

(defn dist-symm-diff [set-1 set-2]
  "the order of the symmetric difference"
  {:pre [(set? set-1) (set? set-2)]}
  (count (set/difference
          (set/union set-1 set-2)
          (set/intersection set-1 set-2))
         ))
