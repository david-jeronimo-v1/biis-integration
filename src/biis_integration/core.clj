(ns biis-integration.core
  (:require [biis-integration.config.input :refer [input]]
            [biis-integration.journey :refer [run-journey]]
            [biis-integration.util :refer [log truncate-folder]]))

(defn -main []
  (truncate-folder "output")
  (let [{:keys [journey payloads log-keys]} input
        run-payload (fn [payload]
                      (-> (run-journey journey payload)
                          :context
                          (#(if log-keys (select-keys % log-keys)
                                         %))))]
    (->> (pmap run-payload payloads)
         (run! log))
    (shutdown-agents)))