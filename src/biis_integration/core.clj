(ns biis-integration.core
  (:use [biis-integration.applied.adjustment]
        [biis-integration.config.input :only [input]]
        [biis-integration.journey :only [run-journey]]
        [biis-integration.util :only [delete-directory-recursive log]])
  (:import (java.io File)))

(defn -main []
  (when (.exists (File. "output"))
    (delete-directory-recursive (File. "output")))
  (.mkdir (File. "output"))
  (let [{:keys [journey payloads log-keys]} input
        run-mta (fn [payload]
                  (-> (run-journey journey payload)
                      :context
                      (#(if log-keys (select-keys % log-keys)
                                     %))))]
    (->> (pmap run-mta payloads)
         (run! log))
    (shutdown-agents)))