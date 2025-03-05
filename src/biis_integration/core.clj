(ns biis-integration.core
  (:require [clojure.java.io :as io])
  (:use [biis-integration.applied-api-connect]
        [biis-integration.applied-rest :only [get-token]]
        [biis-integration.paysafe :only [paysafe-auth]]
        [clojure.pprint :only [pprint]]
        [biis-integration.config :only [home-mta-config]])
  (:import (java.io File)))

(defn home-mta-journey [context]
  (get-home-policy context)
  (let [{request-id "id"} (start-policy-adjustment context)
        context (assoc context :request-id request-id)]
    (update-cover-details context)
    (let [temporary-quote (get-temporary-quote context)
          context (assoc context :cache-id (get temporary-quote "cacheId")
                                 :temporary-quote-id (get-in temporary-quote ["quotes" 0 "temporaryId"]))
          {quote-id "quoteId"} (save-adjustment context)
          context (assoc context :quote-id quote-id)
          {auth-code "authCode" transaction-id "id"} (paysafe-auth context)
          context (assoc context :auth-code auth-code
                                 :transaction-id transaction-id)]
      (accept-adjustment context))))

(defn delete-directory-recursive [^File file]
  (when (.isDirectory file)
    (run! delete-directory-recursive (.listFiles file)))
  (io/delete-file file))

(defn -main []
  (delete-directory-recursive (File. "output"))
  (.mkdir (File. "output"))
  (let [{:keys [policy-codes]} home-mta-config
        {token "access_token"} (get-token)
        run-mta (fn [policy-code]
                  (.mkdir (File. (str "output/" policy-code)))
                  (try (home-mta-journey {:token       token
                                          :policy-code policy-code})
                       (catch Exception e (str "Error " policy-code (.getMessage e)))))]
    (pmap run-mta policy-codes)
    (shutdown-agents)))
