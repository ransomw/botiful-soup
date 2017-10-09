(ns mibot.preproc.graph
  (:require
   [taoensso.tufte :as tufte]

   [clojure.set :as set]
   [loom.graph
    :refer [weighted-graph
            add-nodes
            nodes
            weight
            build-graph
            add-edges
            ]
    :rename {weight edge-weight
             nodes get-nodes
             }]
   [loom.alg :refer [maximal-cliques]]
   [mibot.util
    :refer [position
            subsets-of-a-set
            symmetric-difference
            ]]
   ))

(defn build-weighted-graph
  [vertex-set weight-fn]
  {:pre [(set? vertex-set)]}
  (let [vertices (vec vertex-set)]
    (->>
     vertices
     (map (juxt
           identity
           (fn [one-vertex]
             (tufte/p
              ::bwg-one-vertex
              (->>
               vertices
               (drop (+ 1 (position one-vertex vertices)))
               (map (juxt
                     identity
                     (fn [another-vertex]
                       {:pre [(= (weight-fn one-vertex another-vertex)
                                 (weight-fn another-vertex one-vertex))]}
                       (weight-fn one-vertex another-vertex)
                       )
                     ))
               (filter (fn [[_ weight]] (and weight (< 0 weight))))
               (into {})
               )
              )
             )
           ))
     (into {})
     weighted-graph)))

;; each element is a clique
(defn graph-set-intersections
  [some-sets & {:keys [scaled-weights]}]
  {:pre [(set? some-sets)]}
  (build-weighted-graph
   some-sets
   (fn [one-set another-set]
     {:pre [(set? one-set) (set? another-set)]}
     (float
      (/ (count (set/intersection one-set another-set))
         (if scaled-weights
           (count (set/union one-set another-set))
           1)))
     )))

;; "ish" b/c this is a dual notion but _not_ the dual of the
;; preceeding graph: each element is a vertex,
;; each edge a count of the sets containing both elements
(defn graph-set-intersections-dualish
  [some-sets & {:keys [elem-filter min-intersections]}]
  {:pre [(set? some-sets)]}
  ;; elem-filter opt might be broken
  (build-weighted-graph
   (apply set/union some-sets)
   (fn [one-elem another-elem]
     (let [num-intersections
           (->> some-sets
                (map (fn [a-set]
                       (and
                        (or (not elem-filter)
                            (or (elem-filter one-elem)
                                (elem-filter another-elem)))
                        (and (contains? a-set one-elem)
                             (contains? a-set another-elem))
                        )))
                (filter identity)
                count)]
       (if min-intersections
         (if (>= num-intersections min-intersections)
           num-intersections 0)
         num-intersections))
     )))

;; maps subsets of the powerset of the union of size at least two
;; to the number of the given sets containing them.  considering
;; these as cliques avoids time/memory costs associated with the
;; powerset
(defn weighted-coincidence-cliques
  [some-sets]
  (let [coincidence-graph (graph-set-intersections-dualish
                           some-sets)
        c-edge-weight (partial edge-weight coincidence-graph)
        coincidence-cliques (->> coincidence-graph
                                 maximal-cliques
                                 (map subsets-of-a-set)
                                 (apply set/union)
                                 (filter #(< 1 (count %)))
                                 set
                                 )
        ]
    (zipmap
     coincidence-cliques
     (->>
      coincidence-cliques
      (map (fn [clique]
             (->> clique
                  subsets-of-a-set
                  (filter #(= 2 (count %)))
                  (map vec)
                  (map (fn [[v w]]
                         (c-edge-weight v w)
                         ))
                  (apply min)
                  )
             ))
      ))
    ))

;; build a dict mapping elements of the power set (subsets of a set)
;; to the number of given sets containing that arbitrary element
(defn get-containing-set-counts [some-sets]
  (tufte/p
   ::supset-counts
   (let [all-elems (apply set/union some-sets)]
     (merge
      (weighted-coincidence-cliques some-sets)
      (zipmap
       (map (fn [some-elem] #{some-elem}) all-elems)
       (map (fn [some-elem]
              (->> some-sets
                   (filter #(contains? % some-elem))
                   count)
              ) all-elems)))
     )
   )
  )

;; when (= el-weight-fn (fn [] 1)), distances between vertices
;; are the same as the order of the symmetric difference
;; BROKEN (probably)
(defn dist-graph-symdif [some-sets el-weight-fn]
  (build-weighted-graph
   (subsets-of-a-set (apply set/union some-sets))
   (fn [one-subset another-subset]
     (let [symm-diff (symmetric-difference one-subset another-subset)
           diff-elem (if (= 1 (count symm-diff))
                       (el-weight-fn (first symm-diff)))]
       (if diff-elem
         (el-weight-fn diff-elem))
       ))
     ))


(defn weight-fn-dist-graph-symdif-info
  [containing-set-counts tot-num-sets one-subset another-subset]
  {:pre [(set? one-subset) (set? another-subset)]}
  (let [symm-diff (symmetric-difference one-subset another-subset)
        symm-diff-set-count (->> symm-diff
                                 (get containing-set-counts))
        ]
    (tufte/p
     ::dg-sd-info-weight-fn-calc
     (if symm-diff-set-count
       (* -1 (Math/log
              (/ symm-diff-set-count tot-num-sets)
              )))
     )
    ))

;; -log P is called "information" for some reason..
;; .. the fact is (log p_1)+...+(log p_n) = log (p_1*...*p_n)
;; and this graph is trying to capture the fact that
;; the probabilities of elemental inclusion are _not_ iid.
;; over arbitrary subsets of the power set
(defn dist-graph-symdif-info-mutual [some-sets]
  (let [containing-set-counts (get-containing-set-counts some-sets)
        tot-num-sets (count some-sets)
        ]
    (build-weighted-graph
     (subsets-of-a-set (apply set/union some-sets))
     (fn [one-subset another-subset]
       (tufte/p
        ::dg-sd-info-weight-fn
        (weight-fn-dist-graph-symdif-info
         containing-set-counts tot-num-sets
         one-subset another-subset)
        )
       ))
    ))

;; this graph is doing something similar to the previous,
;; but it doesn't handle mutual occurance probabilities
;; and makes other attempts to optimize around the issue
;; of mapping over the (order 2^n) powerset of elements
(defn dist-graph-symdif-info-single [some-sets]
  (let [containing-set-counts
        (->> (get-containing-set-counts some-sets)
             (map (fn [[subset set-count]]
                    (if (= 1 (count subset))
                      [(first subset) set-count])))
             (into {}))
        all-elems (apply set/union some-sets)
        odd-powerset (set (filter #(odd? (count %))
                                  (subsets-of-a-set all-elems)))
        ]
    (apply
     weighted-graph
     (concat
      (->> odd-powerset
           (map (fn [odd-subset]
                  (map (fn [odd-subset-elem]
                         [odd-subset
                          (set/difference odd-subset
                                          #{odd-subset-elem})
                          (get containing-set-counts
                               odd-subset-elem)
                          ])
                       odd-subset)))
           (apply concat))
      (->> (set/difference odd-powerset all-elems)
           (map (fn [odd-subset]
                  (map (fn [another-elem]
                         [odd-subset
                          (set/union odd-subset
                                     #{another-elem})
                          (get containing-set-counts another-elem)
                          ])
                       (set/difference all-elems (set odd-subset)))
                  ))
           (apply concat)
           )
      ))
    ))

(def dist-graph-symdif-info
  dist-graph-symdif-info-mutual
  )
