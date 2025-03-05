(ns biis-integration.applied-rest
  (:require [clj-http.client :as client])
  (:use [biis-integration.config :only [applied-config]]
        [biis-integration.rest-client :only [send-request]]))

(def base-url "https://api-dev.apigee.appliedcloudservices.co.uk/v1")

(defn get-token []
  (let [{:keys [username password]} applied-config]
    (send-request {:title        "getAppliedToken"
                   :url          (str base-url "/auth/connect/token")
                   :client-f     client/post
                   :body         "grant_type=client_credentials"
                   :username     username
                   :password     password
                   :content-type :x-www-form-urlencoded})))
