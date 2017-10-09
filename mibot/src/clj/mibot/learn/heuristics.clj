(ns mibot.learn.heuristics
  (:require
   [clojure.set :as set]

   [mibot.util :refer [symmetric-difference]]
   [mibot.preproc.graph
    :refer [get-containing-set-counts
            ]]
   ))

(defn greedy-partitioner-h
  [some-subsets-desc some-set-rem curr-partition]
  (if (= 0 (count some-set-rem))
    curr-partition
    (let [contained-subsets-desc
          (->> some-subsets-desc
               (filter (partial set/superset? some-set-rem)))
          add-subset (first contained-subsets-desc)]
      (if add-subset
        (greedy-partitioner-h
         (rest contained-subsets-desc)
         (set/difference some-set-rem add-subset)
         (set/union curr-partition #{add-subset}))
        (set/union curr-partition #{some-set-rem})
        ))
    ))

;; parition for a partition
(defn greedy-partitioner
  [some-subsets some-set
   & {:keys [allow-incomplete]}]
  {:pre [(set? some-set) (set? some-subsets)
         (= 0 (->> some-subsets
                   (map set?) (filter not) count))
         ]
   :post [(or allow-incomplete
              (= 0 (->> % (map (partial contains? some-subsets))
                        (filter not) count))
              )]}
  (greedy-partitioner-h
   (sort #(> (count %1) (count %2)) some-subsets)
   some-set
   (list)))

(defn make-greedy-symdif-info [some-sets]
  (let [tot-num-sets (count some-sets)
        set-counts (get-containing-set-counts some-sets)]
    (fn [set-1 set-2]
      (let [symm-diff (symmetric-difference set-1 set-2)
            symm-diff-partition (greedy-partitioner
                                 (set (keys set-counts)) symm-diff
                                 :allow-incomplete true
                                 )]
        (->> symm-diff-partition
             (map (partial get set-counts))
             (map #(/ % tot-num-sets))
             (map #(Math/log %))
             (map #(* -1 %))
             (reduce +)
             )))
    ))

(defn greedy-symdif-info [some-sets set-1 set-2]
  ((make-greedy-symdif-info some-sets) set-1 set-2)
  )
