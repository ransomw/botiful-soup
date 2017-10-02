(ns mibot.comms.core
  (:require
   [mibot.comms.slack.component :refer [new-comm]
    :rename {new-comm new-slack-comm-private}]
   ))

(def new-slack-comm new-slack-comm-private)
