(ns pa-sand
  (:require
   [clojure.string :as string]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [me.raynes.fs :as fs]
   [postagga.en-fn-v-model :refer [en-model]]
   [postagga.tagger :refer [viterbi]]
   [postagga.parser :refer [parse-tags-rules]]

   [mibot.preproc.nlp.core :refer [lemmatize]]
   [mibot.preproc.nlp.parse :refer [pos-chunk-rules]]

   ))

(def all-pos (:states en-model))

(def pos-tagger (partial viterbi en-model))

(def tag-line (comp
               pos-tagger
               #(string/split % #"\s")
               lemmatize))

(defn parse-line [tagger rules line]
  (parse-tags-rules
   #(string/split % #"\s")
   tagger rules line))

(def q-rules-pos
  [
   {:id :interrogative
    ;; :optional-steps [#{#{"JJ"}}]
    :rule [:question-word
           #{#{"WP"} ;; when where why how
             #{"WRB"} ;; who what
             }
           :topic
           #{#{"VV"}
             #{"VB"}
             #{"VBZ"}
             }
           #{:get-value #{"NN"}
             }
           ]
    }
   {:id :suggestive
    :optional-steps [:topic]
    :rule [:question-word
           #{#{"MD"} ;; would could
             }
           :topic
           #{:get-value #{"NN"}
             }
           ]
    }
   ])

(def q-rules-pos-chunked
  [
   {:id :interrogative
    :rule [:question-word
           #{#{"W"}
             }
           #{#{"V"}}
           :topic
           #{:get-value #{"NN"}
             }
           ]
    }
   {:id :suggestive
    :optional-steps [:topic]
    :rule [:question-word
           #{#{"MD"} ;; would could
             }
           :topic
           #{:get-value #{"NN"}
             }
           ]
    }
   ])

(def pl-q-pos (partial parse-line pos-tagger q-rules-pos))
(def pw-pos (partial parse-line pos-tagger pos-chunk-rules))
