(ns mibot.preproc.nlp.core
  (:import  [lemmatizer StanfordLemmatizer])
  (:require
   [clojure.string :as string]
   [stemmers.core :refer [stems]]
   ))

(defonce core-nlp (delay (StanfordLemmatizer.)))

(defn lemmatize [^String text]
  (let [^StanfordLemmatizer nlp @core-nlp]
    (->> (.lemmatize nlp text)
         (map (partial string/join " "))
         (string/join "\n")
         (string/lower-case)
         )))

(defn stemmatize [text]
  (->> text lemmatize stems
       (string/join " ")))

;; remove oft-used words

;; parse (?)
