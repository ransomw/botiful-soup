(ns mibot.preproc.nlp.parse
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [postagga.en-fn-v-model :refer [en-model]]
   [postagga.tagger :refer [viterbi]]
   [postagga.parser :refer [parse-tags-rules]]

   [mibot.preproc.nlp.dat :refer [pos-chunks]]
   ))

;; (defn set-to-single-elem-sets [aset]
;;   (->> aset
;;        (map (fn [elem] #{elem}))
;;        (into #{})
;;        ))

(def pos-chunk-rules
  (->>
   pos-chunks
   (map
    (fn [[chunk-tag-kw pos-tag-set]]
      {:id chunk-tag-kw
       :rule [:word
              (into #{:get-value}
                    (set (map (fn [str] #{str}) pos-tag-set)))
              ]}))
   ))

(defn pos-chunks-tagger [word]
  (-> (parse-tags-rules
       #(string/split % #"\s")
       (partial viterbi en-model)
       pos-chunk-rules word)
      :result :rule name
      ))

(defn pos-chunks-parser [line]
  (->> (string/split line #"\s")
       (map pos-chunks-tagger)
       ;; (map (juxt identity pos-chunks-tagger))
       ;; (map (partial (string/join "#")))
       ))


;; (def two-grams-rule
;;   (let [all-tags (map name (keys pos-chunks))]
;;     ;; {:id :gram$2
;;     (map

;;      identity
;;      ;; (set-to-single-elem-sets all-tags)

;;          two-grams)
;;     ))
