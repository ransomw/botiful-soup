(ns mibot.bots.echo.core
  (:require
   [clojure.core.async :refer [go close! go-loop <! >!]]
   ))

(defn get-output [cmd-ns input]
  (if (= \, (first input))
    (let [
          ;; curr-ns-name (ns-name *ns*)
          expr (read-string (subs input 1))]
      (in-ns cmd-ns)
      (eval expr)
      ;; (in-ns curr-ns-name)
      )
    input))


(defn get-output-echo-only [_ input]
  input)
