(ns mibot.preproc.parse
  (:require
   [clojure.string :as string]
   [me.raynes.fs :as fs]
   ))

;; read in the directory and return two lists:
;; answers as written and lists of answers,

(defn list-dir [path]
  (map #(-> % .getPath) (fs/list-dir path)))

(defn read-aq-files [root-dir]
  (->>
   (list-dir root-dir)
   (filter fs/directory?)
     (map
      (juxt identity
            (fn [cat-dir]
              (->>
               (list-dir cat-dir)
               (filter #(= ".txt" (fs/extension %)))
               (map (juxt identity slurp))
               (into {})
               )
              )))
     (into {})
     )
   )

(defn parse-question [question]
  (-> question
      (clojure.string/replace #"[^A-z\s]" "")
      (clojure.string/replace #"\s+" " ")
      (string/lower-case)
      ))

(defn parse-aqs [aqs-str]
  (let [[answer & questions]
        (string/split (string/trim aqs-str) #"\n")]
    {:answer answer
     :questions (map parse-question questions)}))

(defn parse-mult-aqs [mult-aqs-str]
  (map parse-aqs (string/split mult-aqs-str #"\n\n")))

(defn parse-aq-files [root-dir-or-filestring-map]
  (if (string? root-dir-or-filestring-map)
    (parse-aq-files (read-aq-files root-dir-or-filestring-map))
    (let [filestring-map root-dir-or-filestring-map]
      (->> filestring-map
           (map (fn [[k v]] [k (if (string? v)
                               (parse-mult-aqs v)
                               (parse-aq-files v))]))
           (into {}))
      )))

;; helper function
(defn nested-vals-h [map-or-val]
  (if (map? map-or-val)
    (map nested-vals-h (vals map-or-val))
    map-or-val))

;; utility function:
;; in case values are lists, this additionally flattens those lists
(defn nested-vals-flat [amap]
  (do (flatten (nested-vals-h amap))))

(defn parse-aq-files-flat [root-dir-or-filestring-map]
  (let [parsed-files ((comp nested-vals-flat parse-aq-files)
                      root-dir-or-filestring-map)]
    {:question-lists (concat (map :questions parsed-files))
     :answers (concat (map :answer parsed-files))
     }))

(defn parsed-aq-to-lists [parsed-files]
  (let [question-lists (concat (map :questions parsed-files))
        answers (concat (map :answer parsed-files))
        ]
    [(flatten question-lists)
     (->> (map list answers question-lists)
          (map (fn [[answer question-list]]
                 (repeat (count question-list) answer)))
          flatten)]
    ))

(def parse-aq-to-lists
  (comp parsed-aq-to-lists nested-vals-flat parse-aq-files))
