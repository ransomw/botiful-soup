(ns mibot.learn.naive-remark
  (:require
   [clojure.set :as set]
   ))

(defn token-expected-values [token-idx-sets token-idx]
  nil) ;; todo

(defn tokens-expected-values [{num-tokens :token num-answers :answer}
                              ;; features each consist of several tokens
                              token-idx-sets]
  {:pre? [(set/subset? (apply set/union token-idx-sets)
                       (range num-tokens))
          (= num-answers (count token-idx-sets))]}
  (->>
   (range num-tokens)
   (map (partial token-expected-values token-idx-sets))
   ))
