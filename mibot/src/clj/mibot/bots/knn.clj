(ns mibot.bots.knn
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [mibot.util :refer [position]]
   [mibot.preproc.parse :as parse]
   [mibot.preproc.nlp.core :refer [lemmatize stemmatize]]
   [mibot.learn.knn :refer [find-nn]]
   [mibot.learn.metrics
    :refer [dist-symm-diff
            dist-symdif-info
            ]]
   [mibot.learn.heuristics
    :refer [
            make-greedy-symdif-info
            greedy-symdif-info
            ]]
   ))

(def aq-root-dir "resources/answer_questions")

(let [[questions answers
       ] (parse/parse-aq-to-lists aq-root-dir)]
  (def questions questions)
  (def answers answers)
  )

(defn preproc-question [question]
  (-> question stemmatize
      (#(string/split % #"\s"))
      set))

(def questions-preproc (do (map preproc-question questions)))

(def words (apply set/union questions-preproc))

(defn preproc-asked-question
  [question & {:keys [keep-words]
               :or {keep-words words}}]
  (set/intersection
   keep-words
   (-> question
       parse/parse-question
       preproc-question)))

(def dist-fns
  {:symm-diff dist-symm-diff
   :info (partial dist-symdif-info (set questions-preproc))
   :info-greedy (make-greedy-symdif-info (set questions-preproc))
   })

(defn get-all-nn-preproc [k dist-fn question]
  {:pre [(contains? (set (keys dist-fns)) dist-fn)
         (integer? k)
         (> k 0)]}
  (let [question-preproc (preproc-asked-question question)
        dist-fn (partial (get dist-fns dist-fn) question-preproc)
        ]
    (find-nn dist-fn questions-preproc k)
    ))

(defn get-answer [k dist-fn question]
  (nth answers
       (->
        (get-all-nn-preproc k dist-fn question)
        ;; in case a question has more that one neighbor
        shuffle first
        (position questions-preproc :all true)
        ;; in case a question corresponds to multiple answers
        shuffle first)
       ))
