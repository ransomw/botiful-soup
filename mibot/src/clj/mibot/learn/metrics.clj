(ns mibot.learn.metrics
  (:require
   [clojure.set :as set]
   [loom.alg :refer [dijkstra-path-dist]]

   [mibot.util :refer [symmetric-difference]]
   [mibot.preproc.graph
    :refer [dist-graph-symdif-info
            ]]
   ))

(defn dist-symm-diff [set-1 set-2]
  "the order of the symmetric difference"
  (count (symmetric-difference set-1 set-2)))

;; this could be amortized if perf is (s)low
(defn dist-symdif-info [some-sets set-1 set-2]
  (let [all-elems (set/union set-1 set-2)
        dist-graph (dist-graph-symdif-info some-sets)
        [path dist
         ] (apply
            (partial dijkstra-path-dist dist-graph)
            (map (partial set/intersection all-elems)
                 [set-1 set-2]))
        ] dist))

