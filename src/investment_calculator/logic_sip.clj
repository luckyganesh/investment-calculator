(ns investment-calculator.logic-sip
  (:require [clojure.string :as string]
            [clojure.data.json :as json]
            [investment-calculator.utils :as u]))

(def csv-keywords [:date :nav])

(defn json-to-clojure-data-converter [filename]
  (json/read-str (slurp filename) :key-fn keyword))

(defn create-sip-records [fund]
  (slurp (str "resources/" fund ".csv")))

(defn map-keywords [keywords data]
  (reduce #(apply assoc %1 %2) {} (map list keywords data)))

(defn create-map-from-csv [csv-string]
  (let [array (map #(string/split % #",") (string/split csv-string #"\n"))
        data (rest array)]
    (map (partial map-keywords csv-keywords) data)))

(defn parse-double [x] (Float/parseFloat x))

(defn read-and-stabilize-data [config]
  (->> (:fund config)
       (create-sip-records)
       (string/trim)
       (create-map-from-csv)
       (map #(update-in % [:nav] parse-double))
       (u/stabilize-dates)))

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

(defn add-actual-worth [previous-record current-record]
  (assoc current-record :actual-worth
                        (+ (* (:nav current-record) (:new-units current-record))
                           (:actual-worth previous-record))))

(defn make-sip []
  (let [config (json-to-clojure-data-converter "config.json")
        records (read-and-stabilize-data config)
        new-units (add-new-units (:initial-amount config) (:amount config) records)
        sip-records (filter #(not= 0 (:new-units %)) new-units)
        total-units (rest (reductions add-total-units {:total-units 0} sip-records))
        total-worth (map add-total-worth total-units)
        actual-worth (rest (reductions add-actual-worth {:actual-worth 0} total-worth))]
    (count (map println actual-worth))))