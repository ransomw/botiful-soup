(ns mibot.preproc.nlp.dat
  (:require
   [clojure.set :as set]
   ))

;; see .../postagga/models/en_penn_tb_tags.md
(def pos-chunks
  {
   :CC #{"CC"} ;; Coordinating conjunction
   :CD #{"CD"} ;; Cardinal number
   :DT #{"DT"} ;; Determiner
   :EX #{"EX"} ;; Existential there
   :FW #{"FW"} ;; Foreign word
   :IN #{"IN"} ;; Preposition or subordinating conjunction
   :J #{
        "JJ" ;; Adjective
        "JJR" ;; Adjective, comparative
        "JJS" ;; Adjective, superlative
        }
   :LS #{"LS"} ;; List item marker
   :MD #{"MD"} ;; Modal
   :N #{
        "NN" ;; Noun, singular or mass
        "NNS" ;; Noun, plural
        "NNP" ;; Proper noun, singular
        "NNPS" ;; Proper noun, plural
        }
   :PDT #{"PDT"} ;; Predeterminer
   :POS #{"POS"} ;; Possessive ending
   :PRP #{
          "PRP" ;; Personal pronoun
          "PRP$" ;; Possessive pronoun
          }
   :R #{
        "RB" ;; Adverb
        "RBR" ;; Adverb, comparative
        "RBS" ;; Adverb, superlative
        }
   :RP #{"RP"} ;; Particle
   :SYM #{"SYM"} ;; Symbol
   :TO #{"TO"} ;; to
   :UH #{"UH"} ;; Interjection
   :V #{
        "VB" ;; Verb, base form
        "VBD" ;; Verb, past tense
        "VBG" ;; Verb, gerund or present participle
        "VBN" ;; Verb, past participle
        "VBP" ;; Verb, non­3rd person singular present
        "VBZ" ;; Verb, 3rd person singular present
        }
   :W #{
        "WDT" ;; Wh­determiner
        "WP" ;; Wh­pronoun
        "WP$" ;; Possessive wh­pronoun
        "WRB" ;; Wh­adverb
        }
   })

;; add words of very high occurence to this list
;; ... and do the stat. n-gram thing for n=2,3ish if there's time
(def one-grams
  ["when" "where" "why" "how" ;; 'WP'
   "who" "what" ;; 'WRB'
   "computer programming"
   ])

(def two-grams
  ["computer programming"
   "in college"
   "where be"
   ])

