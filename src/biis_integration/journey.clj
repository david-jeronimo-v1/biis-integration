(ns biis-integration.journey
  (:require [biis-integration.applied.adjustment :refer [accept-adjustment get-temporary-quote save-adjustment
                                                         start-policy-adjustment update-cover-details]]
            [biis-integration.applied.auth :refer [get-token]]
            [biis-integration.applied.home :refer [accept-quote get-home-policy]]
            [biis-integration.paysafe :refer [paysafe-auth]]
            [biis-integration.util :refer [create-folder enrich enrich-when log truncate-folder]]
            [biis-integration.wallet.auth :refer [get-wallet-token]]
            [biis-integration.wallet.home :refer [create-quote]])
  (:import (clojure.lang ExceptionInfo)))

(defn run-journey [journey context]
  (create-folder "output")
  (let [{:keys [output-folder]} context]
    (truncate-folder (str "output/" output-folder))
    (try {:context (journey context)}
         (catch ExceptionInfo e
           (let [{:keys [response context]} (ex-data e)]
             (log "Error " e)
             {:context       context
              :error-message (ex-message e)
              :error         response})))))

(defn full-mta-journey [context]
  (-> (enrich
        context
        get-wallet-token {:token ["AuthenticationResult" "AccessToken"]}
        create-quote {:policy-code  [0 "policy_code"]
                      :request-id   [0 "policy_id"]
                      :quote-id     [0 "quote_reference"]
                      :quote-amount [0 "cover_premium"]
                      :amount       [0 "cover_premium"]}
        paysafe-auth {:auth-code      "authCode"
                      :transaction-id "id"}
        get-token {:token "access_token"}
        accept-quote nil
        start-policy-adjustment {:request-id "id"}
        update-cover-details nil
        get-temporary-quote {:cache-id           "cacheId"
                             :temporary-quote-id ["quotes" 0 "temporaryId"]
                             :amount             ["quotes" 0 "premium"]}
        save-adjustment {:quote-id "quoteId"})
      (enrich-when (comp pos? :amount)
                   paysafe-auth {:auth-code      "authCode"
                                 :transaction-id "id"}
                   accept-adjustment nil)))

(defn mta-adjust [context]
  (enrich
    context
    get-token {:token "access_token"}
    get-home-policy {:quote-amount ["quote" "premium"]}
    start-policy-adjustment {:request-id "id"}
    update-cover-details nil
    get-temporary-quote {:amount ["quotes" 0 "premium"]}))
