(ns investment-calculator.core
  (:require [investment-calculator.logic-sip :as logic-sip]
            [investment-calculator.logic-SMA :as logic-sma]))

(defn -main [& args]
  (let [config (logic-sip/json-to-clojure-data-converter "config.json")]
    (logic-sma/make-SMA config)))