(ns biis-integration.rest-client
  (:require [clojure.data.json :refer [read-str write-str]]
            [clojure.pprint :refer [pprint]])
  (:use [biis-integration.util :only [log]]))

(def defaultContext {:content-type :json
                     :debug        false})

(defn send-request [context]
  (let [{:keys [url client-f policy-code token body
                debug username password title content-type]} (into defaultContext context)
        client-config {:oauth-token      token
                       :basic-auth       (when username [username password])
                       :body             (if (= content-type :json)
                                           (some-> body write-str)
                                           body)
                       :debug            debug
                       :content-type     content-type
                       :throw-exceptions false}]

    (log (str policy-code " " title))
    (spit (str "output/" policy-code "/" title "_request.txt")
          (with-out-str (pprint body)))

    (let [response (-> (client-f url client-config))
          {:keys [status]} response
          response-json (some-> response :body read-str)]
      (case status
        200 (do (spit (str "output/" policy-code "/" title "_response.txt")
                      (with-out-str (pprint response-json)))
                response-json)
        (throw (ex-info (str title " " status)
                        (or response-json response)))))))
