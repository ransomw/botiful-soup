(ns graph-sand
  (:require
   [clojure.set :as set]
   [loom.io]
   [loom.graph :refer [weight]]
   [loom.alg :refer [dijkstra-path-dist]]

   [mibot.util :refer [position]]
   [mibot.preproc.graph :as graph]
   [mibot.util :refer [subsets-of-a-set]]
   ))


(def some-sets #{
                 (set (range 3))
                 ;; (set (range 2))
                 ;; (set (range 2 5))
                 ;; #{1 2}
                 ;; #{0 2}
                 })

(defn graph-stats [& {:keys [some-set some-elem]}]
  (let [pairs-of-sets (->> some-sets subsets-of-a-set
                           (filter #(= 2 (count %))))
        sd-graph (graph/dist-graph-symdif-info some-sets)
        all-elems (apply set/union some-sets)
        dist (partial dijkstra-path-dist sd-graph)
        ]
    {:pairwise-dist
     (->> pairs-of-sets
          (map vec)
          (map (fn [[w v]] (dist w v)))
          )
     :dist-empty-all
     (dist #{} all-elems)
     :dist-to-empty
     (map (partial dist #{}) some-sets)
     :dist-to-all
     (map (partial dist all-elems) some-sets)
     :dist-to-some-set
     (if some-set
       (map (partial dist some-set) some-sets))
     }))

(defn set-counts []
  (graph/get-containing-set-counts some-sets))

(defn view-coinc-graph []
  (loom.io/view
   (graph/graph-set-intersections-dualish some-sets)
  ))

(defn view-dist-graph []
  (loom.io/view
   (graph/dist-graph-symdif-info some-sets)
   ))


(defn view-dist-graph-single []
  (loom.io/view
   (graph/dist-graph-symdif-info-single some-sets)
   ))

(def vg view-dist-graph-single)
