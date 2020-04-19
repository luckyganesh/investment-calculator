(ns investment-calculator.core
  (:require [clojure.data.json :as json]
            [clojure.string :as string])
  )

(def csv-keywords [:date :nav])

(defn json-to-clojure-data-converter [json-reader filename]
  (json/read-str (json-reader filename) :key-fn keyword))

(defn create-sip-records [file-reader fund]
  (file-reader (str "resources/" fund ".csv")))

(defn map-keywords [keywords data]
  (reduce #(apply assoc %1 %2) {} (map list keywords data)))

(defn create-map-from-csv [csv-string]
  (let [array (map #(string/split % #",") (string/split csv-string #"\n"))
        data (rest array)]
    (map (partial map-keywords csv-keywords) data)))

(defn parse-double [x] (Float/parseFloat x))

(defn new-units-decision [amount {date :date nav :nav}]
  (if (= (subs date 8) "01")
    (/ amount nav)
    0))

(defn buy-new-units [amount sip-record]
  (assoc sip-record :new-units (new-units-decision amount sip-record)))

(defn add-new-units [initial-amount amount records]
  (let [first-record (first records)
        first-nav (:nav first-record)
        remaining-records (rest records)]
    (conj (map (partial buy-new-units amount) remaining-records)
          (assoc first-record :new-units
                              (+ (/ initial-amount first-nav)
                                 (new-units-decision amount first-record))))))

(defn add-total-units [previous-record current-record]
  (assoc current-record :total-units
                        (+ (:new-units current-record)
                           (:total-units previous-record))))

(defn add-total-worth [record]
  (assoc record :total-worth (* (:nav record) (:total-units record))))

(defn -main [& args]
  (let [config (json-to-clojure-data-converter slurp "config.json")
                 records (->> (:fund config)
                              (create-sip-records slurp)
                              (string/trim)
                              (create-map-from-csv)
                              (map #(update-in % [:nav] parse-double)))
                 new-units (add-new-units (:initial-amount config) (:amount config) records)
                 sip-records (filter #(not= 0 (:new-units %)) new-units)
                 total-units (rest (reductions add-total-units {:total-units 0} sip-records))
                 total-worth (map add-total-worth total-units)]
               (count (map println total-worth))))