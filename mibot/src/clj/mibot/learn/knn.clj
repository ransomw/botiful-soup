(ns mibot.learn.knn)

;; this is _not_ the k-nearest-neighbors algorithm
;; for a vector space.  it find the nearest points
;; among a finite subset of a metric space.
(defn find-nn [dist-to-elem-fn other-elems k]
  (->> other-elems
       (map (juxt identity dist-to-elem-fn))
       (sort (fn [[_ a] [_ b]] (< a b)))
       (map first)
       (take k)
       set
       ))
