(ns biis-integration.applied.auth
  (:require [biis-integration.config.config :refer [applied-config]]
            [biis-integration.rest-client :refer [send-request]]
            [clj-http.client :as client]))

(def base-url "https://api-dev.apigee.appliedcloudservices.co.uk/v1")

(defn get-token [context]
  (let [{:keys [username password]} applied-config]
    (-> (assoc context :title "getAppliedToken"
                       :url (str base-url "/auth/connect/token")
                       :client-f client/post
                       :body "grant_type=client_credentials"
                       :username username
                       :password password
                       :content-type :x-www-form-urlencoded)
        send-request)))
