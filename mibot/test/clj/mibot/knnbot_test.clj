(ns mibot.knnbot-test
  (:require
   [clojure.test :refer :all]
   [clojure.set :as set]
   [clojure.string :as string]

   [mibot.util :refer [position]]
   [mibot.preproc.graph
    :refer [get-containing-set-counts
            ]]
   [mibot.learn.heuristics
    :refer [greedy-partitioner
            ]]
   [mibot.learn.knn :refer [find-nn]]
   [mibot.bots.knn :as knnbot]
   ))

(def some-questions
  (let [tot-num-questions (count knnbot/questions)
        some-num-questions 4]
    (->> (range 0 tot-num-questions
                (/ tot-num-questions some-num-questions))
         (take some-num-questions) ;; rounding
         (map (partial nth knnbot/questions))
         )))

(deftest find-nn-test
  (is (= (set (range 3))
         (find-nn identity (set (range 10)) 3)
         ))
  (is (= (set (range 9 6 -1))
         (find-nn #(* -1 (- % 12)) (set (range 10)) 3)
         ))
  )

(deftest data-setup
  (is (apply = (map count [knnbot/questions
                           knnbot/answers
                           knnbot/questions-preproc
                           ])))
  )

(deftest check-containing-set-counts
  (let [containing-set-counts (get-containing-set-counts
                               (set knnbot/questions-preproc))
        containing-sets (set (keys containing-set-counts))]
    (is (= 0
           (->> knnbot/words
                (map (fn [word] #{word}))
                (filter
                 (comp not (partial contains? containing-sets)))
                count
                )))
    ))

(deftest greedy-partitioner-test
  (let [containing-set-counts (get-containing-set-counts
                               (set knnbot/questions-preproc))
        containing-sets (set (keys containing-set-counts))
        some-words (set (take 4 knnbot/words))]
    (greedy-partitioner containing-sets some-words)
    ))

(defn smoke-test-by-dist-fn [dist-fn]
  (let [get-answer (partial knnbot/get-answer 1 dist-fn)]
    (loop [question-idx 0]
      (let [some-question (nth some-questions question-idx)
            some-answer (get-answer (str some-question "?"))
            answer-positions (position some-answer
                                       knnbot/answers :all true)
            question-positions (position some-question
                                         knnbot/questions :all true)
            qna-positions (set/intersection
                           (set answer-positions)
                           (set question-positions))
            ]
        (is (= 1 (count (knnbot/get-all-nn-preproc
                         1 dist-fn
                         some-question))))
        (is (not (= 0 (count qna-positions))))
        )
      (let [next-question-idx (inc question-idx)]
        (if (< next-question-idx (count some-questions))
          (recur next-question-idx)))
      )))

(deftest smoke-test
  (smoke-test-by-dist-fn :symm-diff)
  (smoke-test-by-dist-fn :info-greedy)
  )
