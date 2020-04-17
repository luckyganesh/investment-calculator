(defproject investment-calculator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [lambdaisland/kaocha "1.0-612"]
                 [org.clojure/data.json "1.0.0"]]
  :user {:dependencies [[com.bhauman/rebel-readline "0.1.4"]]
         :aliases {"rebl" ["run" "-m" "rebel-readline.main"]}}
  :aliases {"kaocha" ["run" "-m" "kaocha.runner"]
            "rebl" ["run" "-m" "rebel-readline.runner"]}
  :repl-options {:init-ns investment-calculator.core})
