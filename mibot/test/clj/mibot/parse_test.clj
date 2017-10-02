(ns mibot.parse-test
  (:require
   [clojure.test :refer :all]

   [mibot.preproc.parse :as parse]

   [me.raynes.fs :as fs]
   ))

(def aq-root-dir "resources/answer_questions")

(deftest aq-read-files
  (let [files-map (parse/read-aq-files aq-root-dir)]
    (is (map? files-map))
    (is (= #{true} (set (map map? (vals files-map)))))
    (is (= #{true}
           (set (map string? (flatten (map vals (vals files-map)))))))
    )
  )

(deftest aq-util
  (is (= #{"asdf" "qwer" "zxcv"}
         (set (parse/nested-vals-flat
               {:asdf "asdf"
                :qwer {:asdf "qwer" :qwer "zxcv"}}))))
  )

(deftest answer-questions
  (let [{questions :question-lists answers :answers}
        (parse/parse-aq-files-flat aq-root-dir)]
    (is (seq? questions))
    (is (seq? answers))
    (is (not (= 0 (count questions))))
    (is (= (count answers) (count questions)))
    (is (= #{true} (set (map string? answers))))
    (is (= #{true} (set (map seq? questions))))
    (is (= #{true} (set (map #(< 0 (count %)) questions))))
    (is (= #{true} (set (map string? (apply concat questions)))))
    ))
