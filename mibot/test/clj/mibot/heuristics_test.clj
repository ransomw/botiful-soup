(ns mibot.heuristics-test
  (:require
   [clojure.test :refer :all]
   [clojure.pprint :refer [pprint]]
   [clojure.set :as set]

   [mibot.learn.heuristics :refer [greedy-partitioner]]
   [mibot.util :refer [partitions-of-a-set]]
   ))

(defn generic-greedy-paritioner-checks
  [some-subsets some-set partition]
  )

(deftest greedy-partitioner-test
  (let [some-subsets #{#{1 2}
                       #{1 2 3}
                       #{4}
                       #{4 5}
                       }
        some-set (apply set/union some-subsets)
        greedy-partition (greedy-partitioner some-subsets some-set)]
    (is (= #{#{1 2 3}
             #{4 5}
             }
           greedy-partition))
    (is (contains? (partitions-of-a-set some-set) greedy-partition))
    (doall (map (fn [p-set]
                  (is (contains? some-subsets p-set))
                  ) greedy-partition))
    )
  (let [some-subsets #{#{1 2}
                       #{1 2 3}
                       #{4}
                       #{4 5}
                       }
        some-set (set (range 6))
        greedy-partition (greedy-partitioner some-subsets some-set
                                             :allow-incomplete true)]
    (is (= #{#{1 2 3}
             #{4 5}
             #{0}
             }
           greedy-partition))
    (is (contains? (partitions-of-a-set some-set) greedy-partition))
    )
  )
