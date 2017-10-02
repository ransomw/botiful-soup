(ns mibot.knnbot-test
  (:require
   [clojure.test :refer :all]
   [clojure.set :as set]
   [clojure.string :as string]

   [mibot.util :refer [position]]
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

(deftest smoke-test
  (loop [question-idx 0]
    (let [some-question (nth some-questions question-idx)
          some-answer (knnbot/get-answer (str some-question "?"))
          answer-positions (position some-answer
                                     knnbot/answers :all true)
          question-positions (position some-question
                                       knnbot/questions :all true)
          qna-positions (set/intersection
                         (set answer-positions)
                         (set question-positions))
          ]
      (is (= 1 (count (knnbot/get-all-nn-preproc some-question))))
      (is (not (= 0 (count qna-positions))))
      )
    (let [next-question-idx (inc question-idx)]
      (if (< next-question-idx (count some-questions))
        (recur next-question-idx)))
  ))
