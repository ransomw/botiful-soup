(ns mibot.util
  (:require
   [clojure.set :as set]
   [clojure.math.combinatorics
    :refer [subsets
            partitions
            ]]
   [taoensso.tufte :as tufte]
   ))


;; http://clojuredocs.org/clojure.core/keep-indexed
(defn position [x coll &
                {:keys [from-end all] :or {from-end false all false}}]
  (let [all-idxs
        (keep-indexed (fn [idx val] (when (= val x) idx)) coll)]
    (cond
      (true? from-end) (last all-idxs)
      (true? all) all-idxs
      :else (first all-idxs))))

;; combinatorics/subsets expects and returns ordered collections
(def subsets-of-a-set
  (comp set (partial map set) subsets vec))

(def partitions-of-a-set
  (comp set (partial map set)
        (partial map #(map set %)) partitions))

(defn symmetric-difference [one-set another-set]
  {:pre [(set? one-set) (set? another-set)]}
  (tufte/p
   ::symmetric-difference
   (let [union (set/union one-set another-set)
         intersection (set/intersection one-set another-set)]
     (set/difference union intersection)
     )
   ;; this takes about the same amount of time for the
   ;; "information distance" graph
   ;; (let [one-diff (set/difference one-set another-set)
   ;;       another-diff (set/difference another-set one-set)]
   ;;   (set/union one-diff another-diff))
   )
  )

(defn maximal-sets-under-inclusion [some-sets]
  {:pre [(= #{true} (set (map set? some-sets)))]}
  (filter (fn [some-set]
            (->> (set/difference some-sets)
                 (filter (partial set/subset? some-set))
                 count (= 1)))
          some-sets))

(defn subsets-containing-a-set [all-elems a-set]
  {:pre [(set? a-set) (set? all-elems)]}
  (->>  (set/difference all-elems a-set)
        subsets-of-a-set
        (map #(set/union a-set %))
        ))
