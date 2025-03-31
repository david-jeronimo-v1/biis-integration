(ns biis-integration.applied.home
  (:require [biis-integration.rest-client :refer [send-request]]
            [clj-http.client :as client]))

(def base-url "https://api-dev.apigee.appliedcloudservices.co.uk/applied-api-connect/v1/home")

(defn get-home-policy [context]
  (let [{:keys [policy-code]} context]
    (-> (assoc context :title "getHomePolicy"
                       :url (str base-url "/policy/" policy-code)
                       :client-f client/get)
        send-request)))

(defn accept-quote [context]
  (let [{:keys [policy-code quote-id auth-code transaction-id]} context]
    (-> (assoc context :title "acceptQuote"
                       :url (str base-url "/acceptance")
                       :client-f client/post
                       :body {:policyCode     policy-code
                              :quoteId        quote-id
                              :paymentDetails {:authorizationCode auth-code
                                               :transactionId     transaction-id}})
        send-request)))
