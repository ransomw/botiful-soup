(defproject mibot "0.0.0-SNAPSHOT"
  :description "My Introduction Bot"
  :dependencies
  [
   [org.clojure/clojure "1.8.0"]
   [org.clojure/core.async "0.3.443"]
   [com.stuartsierra/component "0.3.2"]
   [http.async.client "1.2.0"]
   [clj-http "3.7.0"]
   [cheshire "5.8.0"]
   [me.raynes/fs "1.4.6"]
   [edu.stanford.nlp/stanford-corenlp "3.5.1"]
   [edu.stanford.nlp/stanford-corenlp "3.5.1" :classifier "models"]
   [edu.stanford.nlp/stanford-parser "3.5.1"]
   [edu.stanford.nlp/stanford-parser "3.5.1" :classifier "models"]
   [postagga "0.2.8"]
   [org.clojure/math.combinatorics "0.1.4"]
   [org.clojure/math.numeric-tower "0.0.4"]
   [ubergraph "0.4.0"]
   [aysylu/loom "1.0.0"]
   [com.hypirion/clj-xchart "0.2.0"]
   ]

  :plugins
  [
   ]

  :min-lein-version "2.7.1"

  :source-paths
  [
   "src/clj"
   ]

  :java-source-paths ["src/java"]
  :javac-options
  [
   "-target" "1.8"
   "-source" "1.8"
   "-Xlint:-options"
   ]

  :test-paths
  [
   "test/clj"
   ]

  :clean-targets
  [:target-path :compile-path
   ]

  :main mibot.app

  :repl-options
  {
   :init-ns user ;; default: main
   :timeout 120000 ;; ms ;; default: 30000
   }

  :profiles
  {:dev
   {:dependencies
    [
     [org.clojure/tools.nrepl "0.2.13"]
     [reloaded.repl "0.2.3"]
     ]

    :plugins
    [
     ]

    :source-paths ["dev"]
    :repl-options {}
    }
   }

  :jvm-opts
  [
   "-server"
   "-Dfile.encoding=utf-8"
   ]
  )
