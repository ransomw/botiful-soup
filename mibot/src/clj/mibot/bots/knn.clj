(ns mibot.bots.knn
  (:require
   [clojure.string :as string]
   [mibot.util :refer [position]]
   [mibot.preproc.parse :as parse]
   [mibot.preproc.nlp.core :refer [lemmatize]]
   [mibot.learn.knn :refer [find-nn]]
   [mibot.learn.metrics :refer [dist-symm-diff]]
   ))

(def aq-root-dir "resources/answer_questions")

(def aq-flat (parse/parse-aq-files-flat aq-root-dir))

(def questions (flatten (:question-lists aq-flat)))

(def answers
  (->>
   aq-flat
   ((juxt :answers :question-lists))
   (apply (partial map list))
   (map (fn [[answer question-list]]
          (repeat (count question-list) answer)))
   flatten))

(defn preproc-question [question]
  (-> question (lemmatize)
      (#(string/split % #"\s"))
      set))

(def questions-preproc (do (map preproc-question questions)))

(def tmp
  (->>
   aq-flat
   ((juxt :answers :question-lists))
   (apply (partial map list))
   ))

(defn get-all-nn-preproc [question]
  (let [question-preproc (-> question
                             parse/parse-question
                             preproc-question)
        dist-fn (partial dist-symm-diff question-preproc)
        ]
    (find-nn dist-fn questions-preproc 1)
    ))

(defn get-nn-preproc [question]
  ;; {:post [(contains? questions-preproc %)]}
  (-> (get-all-nn-preproc question)
      ;; in case a question has more that one neighbor
      shuffle first))

(defn get-answer [question]
  (nth answers
       (-> question
           get-nn-preproc
           (position questions-preproc :all true)
           ;; in case a question corresponds to multiple answers
           shuffle first)
       ))
