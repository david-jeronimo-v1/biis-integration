(ns biis-integration.rest-client
  (:require [biis-integration.util :refer [log]]
            [clojure.data.json :refer [read-str write-str]]
            [cheshire.core :refer [generate-string]]
            [clojure.pprint :refer [pprint]]))

(def defaultContext {:content-type  :json
                     :output-format :edn
                     :debug         false})

(defn write-output [output-folder output-format title body]
  (let [extension (name output-format)
        file-name (str "output/" output-folder "/" title "." extension)]
    (->> (if (= output-format :json)
           (generate-string body {:pretty true})
           (with-out-str (pprint body)))
         (spit file-name))))

(defn send-request [context]
  (let [{:keys [url client-f policy-code token body headers
                debug username password title content-type
                output-folder output-format]} (into defaultContext context)
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
    (write-output output-folder output-format (str title "_request") body)

    (let [response (-> (client-f url client-config))
          {:keys [status]} response
          try-parse (fn [str] (try (read-str str)
                                   (catch Exception _ response)))
          response-json (some->> response :body try-parse)]
      (case status
        200 (do (write-output output-folder output-format (str title "_response") response-json)
                response-json)
        (throw (ex-info (str title " " status)
                        {:response (or response-json response)
                         :context  context}))))))
