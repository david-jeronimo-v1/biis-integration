(ns biis-integration.applied-api-connect
  (:require [clj-http.client :as client])
  (:use [biis-integration.rest-client :only [send-request]]))

(def base-url "https://api-dev.apigee.appliedcloudservices.co.uk/applied-api-connect/v1")

(defn get-home-policy [context]
  (let [{:keys [policy-code]} context]
    (-> (assoc context :title "getHomePolicy"
                       :url (str base-url "/home/policy/" policy-code)
                       :client-f client/get)
        send-request)))

(defn start-policy-adjustment [context]
  (let [{:keys [policy-code]} context]
    (-> (assoc context :title "startPolicyAdjustment"
                       :url (str base-url "/home/adjustment/request/" policy-code)
                       :client-f client/post)
        send-request)))

(defn update-cover-details [context]
  (let [{:keys [policy-code request-id]} context]
    (-> (assoc context :title "updateCoverDetails"
                       :url (str base-url "/home/adjustment/cover-details")
                       :client-f client/patch
                       :body {:policyCode policy-code
                              :requestId  request-id
                              :request    {:voluntaryExcessAmount 0
                                           :buildingsCoverAmount  450000.00
                                           :contentsCoverAmount   50000.00
                                           :unspecifiedRiskAmount 3000.00
                                           :homeOfficeCoverAmount nil,
                                           :smallCraftCoverAmount nil}})
        send-request)))

(defn get-temporary-quote [context]
  (let [{:keys [policy-code request-id]} context
        result (-> (assoc context :title "getTemporaryQuote"
                                  :url (str base-url "/home/adjustment/quote")
                                  :client-f client/post
                                  :body {:policyCode policy-code
                                         :requestId  request-id
                                         :StartDate  "2025-04-11T00:00:00"
                                         :StartTime  "2025-04-11T00:00:00"})
                   send-request)]
    (if-let [refusal-reasons (seq (get-in result ["quotes" 0 "refusalReasons"]))]
      (throw (ex-info "Temporary quote refused" refusal-reasons))
      result)))

(defn save-adjustment [context]
  (let [{:keys [policy-code cache-id temporary-quote-id]} context]
    (-> (assoc context :title "saveAdjustment"
                       :url (str base-url "/home/adjustment/policy/" policy-code)
                       :client-f client/post
                       :body {:cacheId          cache-id
                              :temporaryQuoteId temporary-quote-id})
        send-request)))

(defn accept-adjustment [context]
  (let [{:keys [policy-code quote-id auth-code transaction-id]} context]
    (-> (assoc context :title "acceptAdjustment"
                       :url (str base-url "/home/adjustment/acceptance")
                       :client-f client/post
                       :body {:policyCode     policy-code
                              :quoteId        quote-id
                              :paymentDetails {:authorizationCode auth-code
                                               :transactionId     transaction-id}
                              })
        send-request)))