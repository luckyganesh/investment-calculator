(ns investment-calculator.core
  (:require [investment-calculator.logic-sip :as logic-sip]
            [investment-calculator.logic-SMA :as logic-sma]))

(defn make-investment [config]
  (condp = (:type config)
    "SMA" (logic-sma/make-SMA config)
    "SIP" (logic-sip/make-sip config)))

(defn filter-record [type record]
  (condp = type
    "SMA" (logic-sma/sma-filter record)
    "SIP" (logic-sip/sip-filter record)))

(defn filter-if-needed [types records]
  (->> (map vector types records)
       (some (partial apply filter-record))))

(defn filter-data [types records]
  (filter (partial filter-if-needed types) records))

(defn calculate-total-worth [record]
  (reduce #(+ %1 (:total-worth %2)) 0 record))

(defn calculate-actual-worth [record]
  (reduce #(+ %1 (:actual-worth %2)) 0 record))

(defn print-record [type record]
  (condp = type
    "SMA" (logic-sma/str-record record)
    "SIP" (logic-sip/str-record record)))

(defn print-day-record [types day-records]
  (->> (mapv print-record types day-records)
       (clojure.string/join ",,")))

(defn str-record [types day-records]
  (str (:date (first day-records)) ",,"
       (print-day-record types day-records) ",,"
       (calculate-actual-worth day-records) ","
       (calculate-total-worth day-records)))

(defn get-header [type]
  (condp = type
    "SMA" logic-sma/headers
    "SIP" logic-sip/headers))

(defn str-headers [types]
  (let [headers
        (->> types
             (map get-header)
             (map #(clojure.string/join "," %))
             (clojure.string/join ",,"))]
    (str "date,," headers ",,actual-worth-of-all-investments,total-worth-of-all-investments")))

(defn -main [& args]
  (let [config (logic-sip/json-to-clojure-data-converter "config.json")
        types (map :type config)
        investment-data (map make-investment config)
        merge-data (apply map vector investment-data)
        filtered-data (filter-data types merge-data)
        str-records (map (partial str-record types) filtered-data)
        add-headers (conj str-records (str-headers types))]
    (with-open [w (clojure.java.io/writer  "./something.csv" :append true)]
      (.write w (clojure.string/join "\n" add-headers)))))