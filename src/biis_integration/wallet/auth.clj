(ns biis-integration.wallet.auth
  (:require [biis-integration.config.config :refer [wallet-config]]
            [biis-integration.rest-client :refer [send-request]]
            [clj-http.client]))

(defn get-wallet-token [context]
  (let [url "https://cognito-idp.eu-west-1.amazonaws.com/oauth2/token"
        {:keys [cognitoClientId username password]} wallet-config]
    (-> (assoc context :title "getWalletToken"
                       :url url
                       :client-f clj-http.client/post
                       :body {:AuthFlow       "USER_PASSWORD_AUTH",
                              :ClientId       cognitoClientId,
                              :AuthParameters {:USERNAME username,
                                               :PASSWORD password},
                              :ClientMetadata {}}
                       :content-type "application/x-amz-json-1.1"
                       :headers {:x-amz-target "AWSCognitoIdentityProviderService.InitiateAuth"})
        send-request)))
