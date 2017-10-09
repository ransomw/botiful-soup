(ns explore-data-nlp
  (:require
   [clojure.string :as string]
   [com.hypirion.clj-xchart :as chart]
   [loom.io]
   [postagga.en-fn-v-model :refer [en-model]]
   [postagga.tagger :refer [viterbi]]
   [mibot.util :refer [position]]
   [mibot.preproc.parse :as parse]
   [mibot.preproc.nlp.core :refer [lemmatize stemmatize]]
   [mibot.preproc.graph :as graph]
   ))


(defn get-word-pos [word]
  (let [pos-tagger (partial viterbi en-model)]
    (first (pos-tagger [word]))
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
                          (map stemmatize)
                          (filter #(not (= "" %)))
                          get-words-info
                          (sort (fn [[_ {a :count}] [_ {b :count}]]
                                  (> a b)))
                          )
        q-words (map first q-words-info)
        q-word-counts (->> q-words-info (map last) (map :count))]
    (chart/view
     (chart/category-chart
      {"Count" [q-words q-word-counts]}
      {:x-axis {:label {:rotation 90}
                :order q-words}}
      ))))

(defn take-subseq [sub-perc start-perc a-seq]
  (let [perc-of (fn [perc a-num] (max 1 (int (* perc a-num))))
        num-elems-tot (count a-seq)
        num-elems-subseq (perc-of sub-perc num-elems-tot)
        ]
    (if start-perc
      (->> a-seq
           (drop (perc-of start-perc num-elems-tot))
           (take num-elems-subseq)
           )
      (->> a-seq
          (take-nth
           (max 1 (int (/ num-elems-tot
                          num-elems-subseq))))
           ))
    ))

;; each word is a clique among vertices of sentences
(defn graph-word-intersections [sub-perc & {:keys [start-perc]}]
  (loom.io/view
   (graph/graph-set-intersections
    (->> questions
         (take-subseq sub-perc start-perc)
         (map stemmatize)
         (filter #(not (= "" %)))
         (map #(string/split % #"\s"))
         (map (partial filter #(not (= "you" %))))
         (map set)
         (set)
         )
    :scaled-weights true)
   ))

(defn build-q-word-coincidence-graph []
  (graph/graph-set-intersections-dualish
   (->> questions
        (map stemmatize)
        (filter #(not (= "" %)))
        (map #(string/split % #"\s"))
        (map set)
        (set)
        )))

(defn graph-q-word-coincidences
  [sub-perc & {:keys [start-perc min-intersections]
               {:keys [include-pos exclude-pos]} :pos-sel
               :or {min-intersections 1
                    pos-sel
                    {:include-pos #{"WP" "WRB" ;; inquiry words
                                    "MD" ;; suggestion
                                    "NP"
                                    }
                     :exclude-pos #{"DT" ;; a, the
                                    "IN" ;; in, on, under
                                    "TO"
                                    "VV"
                                    "JJ" "JJS" ;; adj forms
                                    }}}}]
  (let [preproc-texts (fn [texts]
                        (->> texts
                             (map stemmatize)
                             (filter #(not (= "" %)))))
        q-lwords-set (->> q-words-list
                          preproc-texts
                          set)
        q-word-bags (->> questions
                         (take-subseq sub-perc start-perc)
                         preproc-texts
                         (map #(string/split % #"\s"))
                         (map (partial filter #(not (= "you" %))))
                         (map set)
                         (set)
                         )]

    (println "viewing graph for stemmatized word bags"
             q-word-bags)

    (loom.io/view
     (graph/graph-set-intersections-dualish
      q-word-bags
      :min-intersections min-intersections
      :elem-filter nil
      ;; (fn [word]
      ;;   (let [word-pos (get-word-pos word)]
      ;;     (and (or (not include-pos)
      ;;              (contains? include-pos word-pos))
      ;;          (or (not exclude-pos)
      ;;              (not (contains? exclude-pos word-pos))))
      ;;     ))
      ))))

(def stats question-stats)

(def plot plot-word-counts)

(def graph
  ;; graph-word-intersections
  graph-q-word-coincidences
  )
