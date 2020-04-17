(ns investment-calculator.core
      (:require [clojure.data.json :as json]))

(defn json-to-clojure-data-converter [json-reader filename]
      (json/read-str (json-reader filename) :key-fn keyword))

(defn -main [& args]
      (println (json-to-clojure-data-converter slurp "config.json")))