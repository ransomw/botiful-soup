(ns mibot.system
  (:require
   [com.stuartsierra.component :as component]
   ))

(defn bot-system [bot comm]
  (component/system-map
   :comm comm
   :bot (component/using bot [:comm])
   ))
