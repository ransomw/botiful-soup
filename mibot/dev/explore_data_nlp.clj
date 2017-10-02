(ns explore-data-nlp
  (:require
   [clojure.string :as string]
   [com.hypirion.clj-xchart :as chart]
   [mibot.preproc.parse :as parse]
   [mibot.preproc.nlp.core :refer [lemmatize]]
   [mibot.util :refer [position]]

   [pa-sand :as pa]
   ))

(defn counts [some-list]
  (->>
   some-list
   (map (juxt
         identity
         (fn [some-val]
           (apply + (map (fn [another-val]
                           (if (= some-val another-val)
                             1 0)) some-list)))))
   (into {})))

(def aq-root-dir "resources/answer_questions")

(def qa-flat (parse/parse-aq-files-flat aq-root-dir))

(def questions (flatten (:question-lists qa-flat)))

(def q-words-list (->> questions
                       (map #(string/split % #"\s"))
                       flatten))

(defn get-words-info [words-list]
  (let [words-setvec (vec (set words-list))
        word-counts (->> words-setvec
                           (map #(position % words-list :all true))
                           (map count))
        ]
    (->> (map list words-setvec word-counts)
         (map (fn [[word & infos]]
                {word (zipmap [:count] infos)}))
         (apply merge)
         )))

;; ignore-word list

(def question-stats
  {
   :num-words
   (->> questions
        (map #(string/split % #"\s"))
        flatten set count)
   :num-lwords
   (->> questions
        (map lemmatize)
        (map #(string/split % #"\s"))
        flatten set count)
   :avg-words-per
   (float (/ (apply + (->> questions
                           (map #(string/split % #"\s"))
                           (map set) (map count)
                           ))
             (count questions)))
   :avg-lwords-per
   (float (/ (apply + (->> questions
                           (map lemmatize)
                           (map #(string/split % #"\s"))
                           (map set) (map count)
                           ))
             (count questions)))
   })

(defn plot-word-counts []
  (let [q-words-info (->> q-words-list
                          (map lemmatize)
                          get-words-info)]
    (chart/view
     (chart/category-chart
      {"Count" [(map first q-words-info)
                (->> q-words-info (map last) (map :count))]}
      {:x-axis {:label {:rotation 90}}}
      ))))


(def stats question-stats)

(def plot plot-word-counts)
