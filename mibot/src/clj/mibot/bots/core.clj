(ns mibot.bots.core
  (:require
   [mibot.bots.component :refer [new-bot]]
   [mibot.bots.knn :refer [get-answer]
    :rename {get-answer get-answer-knn}]
   ))

(defn new-echo-bot []
  (new-bot identity))

(defn new-knn-bot []
  (new-bot get-answer-knn))
