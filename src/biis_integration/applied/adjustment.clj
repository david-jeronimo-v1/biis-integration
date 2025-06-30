(ns biis-integration.applied.adjustment
  (:require [biis-integration.rest-client :refer [send-request]]
            [biis-integration.util :refer [deep-merge]]
            [clj-http.client :as client]))

(def base-url "https://api-dev.apigee.appliedcloudservices.co.uk/applied-api-connect/v1/home/adjustment")

(defn start-policy-adjustment [context]
  (let [{:keys [policy-code]} context]
    (-> (assoc context :title "startPolicyAdjustment"
                       :url (str base-url "/request/" policy-code)
                       :client-f client/post)
        send-request)))

(defn update-cover-details [context]
  (let [{:keys [policy-code request-id update-cover-details-override]} context]
    (-> (assoc context :title "updateCoverDetails"
                       :url (str base-url "/cover-details")
                       :client-f client/patch
                       :body (deep-merge {:policyCode policy-code
                                          :requestId  request-id}
                                         update-cover-details-override))
        send-request)))

(defn update-contact-details [context]
  (let [{:keys [policy-code request-id update-contact-details-override]} context]
    (-> (assoc context :title "updateContactDetails"
                       :url (str base-url "/proposer/contact-details")
                       :client-f client/patch
                       :body (deep-merge {:requestId  request-id
                                          :policyCode policy-code}
                                         update-contact-details-override))
        send-request)))

(defn get-temporary-quote [context]
  (let [{:keys [policy-code request-id temp-quote-start]} context
        result (-> (assoc context :title "getTemporaryQuote"
                                  :url (str base-url "/quote")
                                  :client-f client/post
                                  :body {:policyCode policy-code
                                         :requestId  request-id
                                         :StartDate  temp-quote-start
                                         :StartTime  temp-quote-start})
                   send-request)]
    (if-let [refusal-reasons (seq (get-in result ["quotes" 0 "refusalReasons"]))]
      (throw (ex-info "Temporary quote refused" {:refusal-reasons refusal-reasons}))
      result)))

(defn save-adjustment [context]
  (let [{:keys [policy-code cache-id temporary-quote-id]} context]
    (-> (assoc context :title "saveAdjustment"
                       :url (str base-url "/policy/" policy-code)
                       :client-f client/post
                       :body {:cacheId          cache-id
                              :temporaryQuoteId temporary-quote-id})
        send-request)))

(defn accept-adjustment [context]
  (let [{:keys [policy-code quote-id auth-code transaction-id]} context]
    (-> (assoc context :title "acceptAdjustment"
                       :url (str base-url "/acceptance")
                       :client-f client/post
                       :body {:policyCode     policy-code
                              :quoteId        quote-id
                              :paymentDetails {:authorizationCode auth-code
                                               :transactionId     transaction-id}
                              }
                       )
        send-request)))