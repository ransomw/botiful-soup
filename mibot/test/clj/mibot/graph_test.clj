(ns mibot.graph-test
  (:require
   [clojure.test :refer :all]
   [clojure.pprint :refer [pprint]]
   [clojure.set :as set]
   [loom.graph
    :refer [nodes
            edges
            weight
            has-edge?
            ]]

   [mibot.util
    :refer [subsets-of-a-set
            partitions-of-a-set
            symmetric-difference
            maximal-sets-under-inclusion
            subsets-containing-a-set
            ]]
   [mibot.preproc.graph :as graph]
   ))

(defn generic-wg-tests [vertex-set weight-fn]
  (let [wg (graph/build-weighted-graph vertex-set weight-fn)
        all-edges (edges wg)
        edge-weight (partial apply (partial weight wg))]
    (is (= (nodes wg) vertex-set))
    (is (= (->> all-edges (map set) set)
           (->> (subsets-of-a-set vertex-set)
                (filter #(= 2 (count %)))
                (map vec)
                (filter #(not (= 0 (apply weight-fn %))))
                (map set) set)))
    (is (= (map edge-weight all-edges)
           (map (partial apply weight-fn) all-edges)))
    ))

(deftest build-weighted-graph
  (let [vertex-set #{0 1 2 3 4}
        weight-fn (fn [v w] (mod (* v w) (count vertex-set)))
        ]
      (generic-wg-tests vertex-set weight-fn)
      )
  (let [vertex-set #{0 1 2 3 4 5}
        weight-fn (fn [v w] (mod (* v w) (count vertex-set)))]
    (generic-wg-tests vertex-set weight-fn)
    )
  (let [vertex-set (subsets-of-a-set (set (range 4)))
        weight-fn (fn [v w] (count (set/intersection v w)))]
    (generic-wg-tests vertex-set weight-fn)
    )
  )


(defn get-info-edges-and-weights [info-sets possible-edges]
  (let [symdifs-of-pairs (->> possible-edges
                              (map vec)
                              (map (partial apply symmetric-difference))
                              )
        sets-containing-symdifs (->>
                                 symdifs-of-pairs
                                 (map
                                  (fn [symdif]
                                    (->> info-sets
                                         (filter
                                          #(set/superset? % symdif))
                                         )
                                    )))
        expected-edges (->>
                        (map list possible-edges
                             sets-containing-symdifs)
                        (filter #(not (= 0 (count (last %)))))
                        (map first))
        expected-weights (->>
                          sets-containing-symdifs
                          (filter #(not (= 0 (count %))))
                          (map #(/ (count %) (count info-sets)))
                          (map #(Math/log %))
                          (map #(* -1 %)))
        ]
    [expected-edges expected-weights]
    ))

(defn get-info-unexpected-edges [some-sets]
  (let [all-elems (apply set/union some-sets)
        complement (partial set/difference all-elems)
        ;; the symmetric differences corresponding to absent edges
        edge-sets (->> some-sets
                       maximal-sets-under-inclusion
                       (map (partial subsets-containing-a-set
                                     all-elems))
                       flatten set
                       (filter #(not (contains? some-sets %)))
                       )
        ]
    (mapcat
     (fn [edge-set]
       (mapcat
        (fn [add-elems]
          (->> (partitions-of-a-set edge-set :min 2 :max 2)
               (map vec)
               (map (fn [[one-subset another-subset]]
                      [(set/union one-subset add-elems)
                       (set/union another-subset add-elems)
                       ]
                      ))
               )
          )
        ((comp subsets-of-a-set complement) edge-set))
       ) edge-sets)
    ))

(defn check-info-weight [info-weight [pairs-edge pairs-weight]]
  (is (= ((comp (partial apply info-weight)
                vec) pairs-edge)
         pairs-weight)))

(deftest symdif-info-dist
  (let [one-set #{0 1}
        another-set #{0 2}
        some-more-sets #{(set (range 3)) (set (range 2 5))}
        some-sets (set/union #{one-set another-set} some-more-sets)
        pairs-of-some-sets (filter #(= 2 (count %))
                                   (subsets-of-a-set some-sets))
        [pairs-edges pairs-weights] (get-info-edges-and-weights
                                     some-sets pairs-of-some-sets)
        [other-edges other-weights] (get-info-edges-and-weights
                                     some-sets
                                     [[#{0 1} #{1}]
                                      [#{0} #{1}]
                                      [#{1 2} #{2 3}]
                                      ])
        info-graph (graph/dist-graph-symdif-info some-sets)
        info-weight (partial weight info-graph)
        ]
    (is (= (count pairs-edges) (count pairs-weights)))
    (doall
     (->> (map list pairs-edges pairs-weights)
          (map (partial check-info-weight info-weight))
          ))
    (doall
     (->> (map list other-edges other-weights)
          (map (partial check-info-weight info-weight))
          ))
    (doall
     (->> (get-info-unexpected-edges some-sets)
          (map (fn [[u v]]
                 (is (not (has-edge? info-graph u v)))
                 ))
          ))
    ))
