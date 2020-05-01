(ns investment-calculator.utils
  (:require [clj-time.format :as f]
            [clj-time.core :as t]))

(defn next-date [day]
  (let [next-day (t/plus (f/parse (f/formatter "YYYY-MM-dd") day) (t/days 1))]
    (f/unparse (f/formatter "YYYY-MM-dd") next-day)))

(defn add-middle-days [previous-day-values current-day-values]
  (loop [previous-values []
         previous-day-values previous-day-values]
    (if (= (:date previous-day-values) (:date current-day-values))
      previous-values
      (recur (conj previous-values previous-day-values) (update-in previous-day-values [:date] next-date)))))

(defn stabilize-dates [dates]
  (let [dates dates
        partitioned-values (partition 2 1 dates)
        add-middle-days (vec (mapcat (partial apply add-middle-days) partitioned-values))
        add-last-day (conj add-middle-days (last dates))]
    add-last-day))