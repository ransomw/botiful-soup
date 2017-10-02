(ns mibot.preproc.graph
  (:require
   [clojure.set :as set]
   [loom.graph :refer [weighted-graph]]
   ))

(defn graph-set-intersections [some-sets & {:keys [scaled-weights]}]
  {:pre [(set? some-sets)]}
  (->>
   some-sets
   (map (juxt
         identity
         (fn [one-set]
           (->>
            (set/difference some-sets #{one-set})
            (map (juxt
                  identity
                  (fn [another-set]
                    (/ (count (set/intersection one-set another-set))
                       (if scaled-weights
                         (count (set/union one-set another-set))
                         1)))
                  ))
            (into {})
            ))
         ))
   (into {})
   weighted-graph))



