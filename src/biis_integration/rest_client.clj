(ns biis-integration.rest-client
  (:require [clojure.data.json :refer [read-str write-str]]
            [clojure.pprint :refer [pprint]])
  (:use [biis-integration.util :only [log]]))

(def defaultContext {:content-type :json
                     :debug        false})

(defn send-request [context]
  (let [{:keys [url client-f policy-code token body headers
                debug username password title content-type
                output-folder]} (into defaultContext context)
        client-config {:oauth-token      token
                       :basic-auth       (when username [username password])
                       :body             (if (contains? #{:json "application/x-amz-json-1.1"} content-type)
                                           (some-> body write-str)
                                           body)
                       :debug            debug
                       :content-type     content-type
                       :headers          headers
                       :throw-exceptions false}]

    (log (str policy-code " " title))
    (spit (str "output/" output-folder "/" title "_request.edn")
          (with-out-str (pprint body)))

    (let [response (-> (client-f url client-config))
          {:keys [status]} response
          try-parse (fn [str] (try (read-str str)
                                   (catch Exception _ response)))
          response-json (some->> response :body try-parse)]
      (case status
        200 (do (spit (str "output/" output-folder "/" title "_response.edn")
                      (with-out-str (pprint response-json)))
                response-json)
        (throw (ex-info (str title " " status)
                        {:response (or response-json response)
                         :context context}))))))
