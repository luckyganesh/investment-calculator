(ns investment-calculator.core
  (:require [investment-calculator.logic-sip :as logic-sip]))


(def my-values [{:date "2020-03-12", :nav 318.3876}
                {:date "2020-03-13", :nav 318.4047}
                {:date "2020-03-14", :nav 318.452}
                {:date "2020-03-15", :nav 318.4986}
                {:date "2020-03-17", :nav 318.5649}
                {:date "2020-03-18", :nav 318.5465}
                {:date "2020-03-19", :nav 318.3051}
                {:date "2020-03-21", :nav 318.3932}
                {:date "2020-03-22", :nav 318.4497}
                {:date "2020-03-23", :nav 318.2253}
                {:date "2020-03-26", :nav 318.2818}
                {:date "2020-03-27", :nav 319.1901}
                {:date "2020-03-28", :nav 319.2373}])

(defn -main [& args]
  (logic-sip/make-sip))