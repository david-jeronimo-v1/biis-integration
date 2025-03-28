(ns biis-integration.wallet.home
  (:require [clj-http.client :as client]
            [clojure.data.json :refer [read-str]]
            [biis-integration.util :refer [deep-merge]])
  (:use [biis-integration.rest-client :only [send-request]]))

(def base-url "https://api.ecommerce.ci.bluegrizzly-biis.io")

(defn create-quote [context]
  (let [override (get-in context [:create-quote-override])
        body (-> (slurp "resources/createQuote.request.json") read-str (deep-merge override))
        result (-> (assoc context :title "createQuote"
                                  :url (str base-url "/home/applied_home_quotes")
                                  :body body
                                  :client-f client/post
                                  :headers {"x-custom-ui-version" "develop/10000"})
                   send-request)]
    (if-let [refusal-reasons (seq (get-in result [0 "acceptance_notifications"]))]
      (throw (ex-info "Quote refused" {:refusal-reasons refusal-reasons}))
      result)))
