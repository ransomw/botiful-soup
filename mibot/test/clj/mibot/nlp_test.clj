(ns mibot.nlp-test
  (:require
   [clojure.test :refer :all]

   [mibot.preproc.nlp.core :as nlp]
   ))

(deftest lemmatize
  (is (= "we" (nlp/lemmatize "us")))
  (is (= "we run" (nlp/lemmatize "us ran")))
  (is (= "i" (nlp/lemmatize "me")))
  )
