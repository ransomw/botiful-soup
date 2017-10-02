(ns mibot.preproc.nlp.core
  (:import  [lemmatizer StanfordLemmatizer])
  (:require
   [clojure.string :as string]
   ))

(defonce core-nlp (delay (StanfordLemmatizer.)))

(defn lemmatize [^String text]
  (let [^StanfordLemmatizer nlp @core-nlp]
    (->> (.lemmatize nlp text)
         (map (partial string/join " "))
         (string/join "\n")
         (string/lower-case)
         )))

;; remove oft-used words

;; parse (?)
