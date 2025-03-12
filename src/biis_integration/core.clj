(ns biis-integration.core
  (:require [clojure.java.io :as io])
  (:use [biis-integration.applied-api-connect]
        [biis-integration.applied-rest :only [get-token]]
        [biis-integration.config :only [home-mta-config]]
        [biis-integration.paysafe :only [paysafe-auth]]
        [biis-integration.util :only [log with-context]])
  (:import (clojure.lang ExceptionInfo)
           (java.io File)))

(defn home-mta-journey [context]
  (with-context context
                get-home-policy nil
                start-policy-adjustment {:request-id "id"}
                update-cover-details nil
                get-temporary-quote {:cache-id           "cacheId"
                                     :temporary-quote-id #(get-in % ["quotes" 0 "temporaryId"])}
                save-adjustment {:quote-id "quoteId"}
                paysafe-auth {:auth-code     "authCode"
                              :transactionId "id"}
                ;accept-adjustment nil
                ))

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
                       (catch ExceptionInfo e
                         (log (str "Error " policy-code)
                              e))))]
    (pmap run-mta policy-codes)
    (shutdown-agents)))
