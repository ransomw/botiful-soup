(ns user
  (:require
   [clojure.test :refer [run-tests test-vars]]
   [clojure.repl :refer [doc]]
   [clojure.edn :as edn]
   [clojure.tools.namespace.repl :refer [set-refresh-dirs refresh]]
   [com.stuartsierra.component :as component]
   [reloaded.repl :refer [system init]]
   [mibot.system :refer [bot-system]]
   [mibot.bots.core :refer
    [new-echo-bot
     new-knn-bot
     ]]
   [mibot.comms.core :refer
    [new-slack-comm
     ]]

   [clojure.string :as s]
   [clojure.set :as set]
   [clojure.core.async :as async]

   [mibot.echobot-test]
   [mibot.slack-test]
   [mibot.parse-test]
   [mibot.nlp-test]
   [mibot.spaceout-test]
   [mibot.knnbot-test]

   [mibot.learn.naive-remark]

   [async-sand]
   [pa-sand :as pa]
   [explore-data-nlp :as edl]

   [postagga.en-fn-v-model :refer [en-model]]
   [mibot.preproc.nlp.core :refer [lemmatize]]
   [mibot.preproc.nlp.parse :as parse]
   [mibot.preproc.nlp.dat :refer [pos-chunks]]
   [mibot.bots.knn :as knnbot]
   [mibot.learn.metrics :refer [dist-symm-diff]]
   [mibot.util :refer [position]]
   ))

(def dev-config-filepath "resources/config/dev.edn")

(def dev-config (-> dev-config-filepath
                    slurp edn/read-string))

(def bot-constructors
  {:echo new-echo-bot
   :knn new-knn-bot
   })

(defn dev-system [bot]
  {pre? [(contains? (keys bot-constructors) bot)
         (contains? (keys dev-config) bot)
         ]}
  (bot-system
   ((get bot-constructors bot))
   (apply new-slack-comm ((juxt :api-token :channel)
                          (get dev-config bot)))
   ))

(set-refresh-dirs "src" "dev" "test")

(def start reloaded.repl/start)
(def stop reloaded.repl/stop)
(defn go [bot]
  (reloaded.repl/set-init! #(dev-system bot))
  (reloaded.repl/go))
(def reset reloaded.repl/reset)
(def reset-all reloaded.repl/reset-all)

(def test-ns-list
  [
   'mibot.echobot-test
   'mibot.slack-test
   'mibot.parse-test
   'mibot.nlp-test
   'mibot.spaceout-test
   'mibot.knnbot-test
   ])

(defn run-all-tests []
  (do
    (stop)
    (refresh)
    (map run-tests test-ns-list)
    ))
