(ns biis-integration.paysafe
  (:require [biis-integration.config.config :refer [paysafe-config]]
            [biis-integration.rest-client :refer [send-request]]
            [clj-http.client :as client]
            [clojure.math :refer [round]]))

(def base-url "https://api.test.paysafe.com/cardpayments/v1")

(defn paysafe-auth [context]
  (let [{:keys [request-id requests amount]} context
        {:keys [fbd-account-number username password card-number]} paysafe-config
        auth-request {:merchantRefNum request-id
                      :amount         (-> amount (* 100) round)
                      :settleWithAuth true
                      :card           {:cardNum    card-number
                                       :cardExpiry {:month 12
                                                    :year  2025}
                                       :cvv        123}
                      :profile        {:firstName "Joe"
                                       :lastName  "Smith"
                                       :email     "Joe.Smith@canada.com"}
                      :billingDetails {:street  "12"
                                       :city    "Toronto"
                                       :state   "ON"
                                       :country "CA"
                                       :zip     "M5H 2N2"}}
        body (->> (get requests :paysafe-auth)
                  (merge auth-request))]
    (-> (assoc context :title "paysafeAuth"
                       :username username
                       :password password
                       :client-f client/post
                       :url (str base-url "/accounts/" fbd-account-number "/auths")
                       :body body)
        send-request)))
