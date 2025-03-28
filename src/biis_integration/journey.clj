(ns biis-integration.journey
  (:require [biis-integration.applied.adjustment :refer [accept-adjustment get-temporary-quote save-adjustment
                                                         start-policy-adjustment update-cover-details]]
            [biis-integration.applied.auth :refer [get-token]]
            [biis-integration.applied.home :refer [accept-quote get-home-policy]]
            [biis-integration.paysafe :refer [paysafe-auth]]
            [biis-integration.util :refer [delete-directory-recursive log with-context]]
            [biis-integration.wallet.auth :refer [get-wallet-token]]
            [biis-integration.wallet.home :refer [create-quote]]
            [clojure.math :refer [round]])
  (:import (clojure.lang ExceptionInfo)
           (java.io File)))

(defn in-cents [amount]
  (-> amount (* 100) round))

; inclevy from wallet createQuote response
(defn extract-wallet-quote-amount [quote-response]
  (some->> (get-in quote-response [0 "premium_details"])
           (filter #(= "INCLEVY" (get % "code")))
           first
           (#(get % "running_total"))
           in-cents))

; premium from applied getHomePolicy response
(defn extract-quote-amount [quote-response]
  (some->> (get-in quote-response ["quote" "premium"])
           in-cents))

; premium from applied getTemporaryQuote response
(defn extract-temporary-quote-premium [quote-response]
  (some->> (get-in quote-response ["quotes" 0 "premium"])
           in-cents))

(defn run-journey [journey context]
  (when-not (.exists (File. "output"))
    (.mkdir (File. "output")))
  (let [{:keys [output-folder]} context
        folder (File. (str "output/" output-folder))]
    (when (.exists folder) (delete-directory-recursive folder))
    (.mkdir folder)
    (try {:context (journey context)}
         (catch ExceptionInfo e
           (let [{:keys [response context]} (ex-data e)]
             (log "Error " e)
             {:context       context
              :error-message (ex-message e)
              :error         response})))))

(defn full-mta-journey [context]
  (let [context (with-context
                  context
                  get-wallet-token {:token #(get-in % ["AuthenticationResult" "AccessToken"])}
                  create-quote {:policy-code  #(get-in % [0 "policy_code"])
                                :request-id   #(get-in % [0 "policy_id"])
                                :quote-id     #(get-in % [0 "quote_reference"])
                                :quote-amount extract-wallet-quote-amount
                                :amount       extract-wallet-quote-amount}
                  paysafe-auth {:auth-code      "authCode"
                                :transaction-id "id"}
                  get-token {:token "access_token"}
                  accept-quote nil
                  start-policy-adjustment {:request-id "id"}
                  update-cover-details nil
                  get-temporary-quote {:cache-id           "cacheId"
                                       :temporary-quote-id #(get-in % ["quotes" 0 "temporaryId"])
                                       :amount             extract-temporary-quote-premium}
                  save-adjustment {:quote-id "quoteId"})]
    (if (-> context :amount pos?)
      (with-context context
                    paysafe-auth {:auth-code      "authCode"
                                  :transaction-id "id"}
                    accept-adjustment nil)
      context)))

(defn mta-adjust [context]
  (with-context
    context
    get-token {:token "access_token"}
    get-home-policy {:quote-amount extract-quote-amount}
    start-policy-adjustment {:request-id "id"}
    update-cover-details nil
    get-temporary-quote {:cache-id           "cacheId"
                         :temporary-quote-id #(get-in % ["quotes" 0 "temporaryId"])
                         :amount             extract-temporary-quote-premium}))
