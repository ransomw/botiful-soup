(ns mibot.learn.spaceout
  (:require
   [clojure.set :as set]
   [clojure.math.combinatorics
    :refer [
            cartesian-product
            subsets
            ]]
   ))

(def set-product (comp set cartesian-product))

(def set-of-subsets (comp set (partial map set) vec subsets))

(defn pairwise-distances [dist subset]
  (map (partial apply dist) (set-product subset subset)))

(defn open-ball [{dist :d space :X} point radius]
  (->> space
       (filter (fn [q] (< (dist q point) radius)))
       ))

(defn induced-top [{dist :d space :X :as metric-space}]
  (let [distances (pairwise-distances dist space)
        space-diam (max distances)
        radii-inc (min distances)
        radii (range radii-inc (+ space-diam radii-inc) radii-inc)
        open-balls
        (->> (set-product space radii)
             (map (apply (partial open-ball metric-space)))
             set)
        ]
    (->> (set-of-subsets open-balls)
         (map (partial apply set/union))
         set)
    ))

(defn diam [dist subset]
  (max (pairwise-distances dist subset)))
