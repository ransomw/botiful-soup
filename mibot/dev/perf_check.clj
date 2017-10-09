(ns perf-check
  (:require
   [clojure.set :as set]
   [clojure.pprint :refer [pprint]]
   [clojure.math.combinatorics :refer [subsets]]
   [loom.io]
   [taoensso.tufte :as tufte
    :refer [defnp profiled profile]]

   [mibot.util :refer [subsets-of-a-set position]]
   [mibot.preproc.graph :as graph]
   [mibot.learn.heuristics
    :refer [greedy-symdif-info
            ]]
   [mibot.learn.knn :refer [find-nn]]
   [mibot.bots.knn :as knnbot]
   ))

(defn build-graphs [& {:keys [q-perc] :or
                       {q-perc 1}}]
  (let [q-num (->> (count knnbot/questions-preproc)
                   (* q-perc) int (max 1))
        ;; each is a set of words
        questions-preproc (->> knnbot/questions-preproc
                               (take q-num) set)
        ]
    (profiled
     {}

     ;; (tufte/p :dg-sd-info
     ;;          (graph/dist-graph-symdif-info questions-preproc)
     ;;          )

     (tufte/p :dg-sd-info-single
              (graph/dist-graph-symdif-info-single questions-preproc)
              )

     ;; (tufte/p :set-counts
     ;;          (graph/get-containing-set-counts questions-preproc)
     ;;          )
     ;; (tufte/p :create-all-subsets
     ;;          (subsets-of-a-set questions-preproc))

     )))



(defn build-graphs-print-stats [ & opts]
  (let [timescale 1000000
        [res stats] (apply build-graphs opts)]

    ;; (pprint stats)

    (println "total time: "
             (float (/ (-> stats :clock :total) timescale))
             )
    (println "times by id")
    (pprint (->> (:id-stats-map stats)
                 (map
                  (fn [[id id-stats]]
                    [id
                     (float (/ (-> id-stats :time) timescale))]))
                 (into {})
                 ))
    ))


(defn do-things-print-stats [ things & opts]
  (let [timescale 1000000
        [res stats] (apply things opts)]

    ;; (pprint stats)

    (println "total time: "
             (float (/ (-> stats :clock :total) timescale))
             )
    (println "times by id")
    (pprint (->> (:id-stats-map stats)
                 (map
                  (fn [[id id-stats]]
                    [id
                     (float (/ (-> id-stats :time) timescale))]))
                 (into {})
                 ))
    ))


(defn get-all-nn-greedy-symdif-info
  [& {:keys [q-perc question]
      :or {q-perc 1}}]
  (let [q-num (->> (count knnbot/questions-preproc)
                   (* q-perc) int (max 1))
        ;; each is a set of words
        questions-preproc (->> knnbot/questions-preproc
                               (take q-num) set)
        words (apply set/union questions-preproc)
        question-to-ask (if question
                          (knnbot/preproc-asked-question
                           question :keep-words words)
                          (first questions-preproc))
        ]
    (profiled
     {}
     (tufte/p
      :find-nn
      (find-nn
       (partial greedy-symdif-info (set questions-preproc)
                question-to-ask)
       questions-preproc 1)
      ))
    ))

(def bg build-graphs-print-stats)

(def gnn (partial do-things-print-stats
                  get-all-nn-greedy-symdif-info))
