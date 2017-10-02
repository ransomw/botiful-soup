(ns mibot.util)

;; http://clojuredocs.org/clojure.core/keep-indexed
(defn position [x coll &
                {:keys [from-end all] :or {from-end false all false}}]
  (let [all-idxs
        (keep-indexed (fn [idx val] (when (= val x) idx)) coll)]
    (cond
      (true? from-end) (last all-idxs)
      (true? all) all-idxs
      :else (first all-idxs))))
