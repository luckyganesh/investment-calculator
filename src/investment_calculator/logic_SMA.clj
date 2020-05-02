(ns investment-calculator.logic-SMA
  (:require [investment-calculator.utils :as u]
            [investment-calculator.logic-sip :as logic-sip]
            [clojure.string :as string]))

(defn read-parse-fund [fund]
  (->> fund
       (logic-sip/create-sip-records)
       (string/trim)
       (logic-sip/create-map-from-csv)
       (map #(update-in % [:nav] logic-sip/parse-double))
       (u/stabilize-dates)))

(defn merge-navs [{date :date nav :nav} {s-nav :nav}]
  {
   :date  date
   :nav   nav
   :s-nav s-nav})

(defn calculate-smas [sma navs]
  (->> navs
       (reverse)
       (partition-all sma 1)
       (map #(/ (apply + %) (count %)))
       (reverse)))

(defn add-smas [sma-days records]
  (let [navs (map :nav records)
        smas (calculate-smas sma-days navs)]
    (map #(assoc %1 :sma %2) records smas)))

; nu - new units of main fund
; tu - total units of main fund
; nau - new units of alternative fund
; tau - total units of alternative fund


(defn new-units-decision [amount date nav]
  (if (= (subs date 8) "01")
    (/ amount nav)
    0))

(defn add-new-units [amount {ptu :tu ptau :tau actual-worth :actual-worth} {date :date nav :nav s-nav :s-nav sma :sma}]
  (let [nub (new-units-decision amount date nav)
        naub (new-units-decision amount date s-nav)
        nu (+ nub (/ (* ptau s-nav) nav))
        nau (+ naub (/ (* ptu nav) s-nav))
        worth (+ actual-worth (if (= (subs date 8) "01") amount 0))
        current-record {:date date :nav nav :s-nav s-nav :sma sma :actual-worth worth}]
    (if (< nav sma)
      (-> current-record
          (assoc :nu (- ptu))
          (assoc :tu 0)
          (assoc :nau nau)
          (assoc :tau (+ nau ptau)))
      (-> current-record
          (assoc :nu nu)
          (assoc :tu (+ nu ptu))
          (assoc :nau (- ptau))
          (assoc :tau 0)))))

(defn add-total-worth [record]
  (assoc record :total-worth (+ (* (:nav record) (:tu record)) (* (:s-nav record) (:tau record)))))

(defn sma-filter [record]
  (not (== 0 (:nu record) (:nau record))))

(defn str-record [{nav :nav s-nav :s-nav nu :nu tu :tu nau :nau tau :tau total-worth :total-worth actual-worth :actual-worth sma :sma}]
  (string/join "," [nav s-nav sma nu tu nau tau actual-worth total-worth]))

(def headers ["nav" "liquid-nav" "sma" "new-units" "total-units" "liquid-new-units" "liquid-total-units" "actual-worth" "total-worth"])

(defn make-SMA [config]
  (let [fund-records (read-parse-fund (:fund config))
        alternative-fund-records (read-parse-fund (:alternative-fund config))
        merged-funds (map merge-navs fund-records alternative-fund-records)
        added-smas (add-smas (:sma config) merged-funds)
        new-units (rest (reductions (partial add-new-units (:amount config)) {:tu (/ (:initial-amount config) (:nav (first added-smas))) :tau 0 :actual-worth (:initial-amount config)} added-smas))
        total-worth (map add-total-worth new-units)]
    total-worth))