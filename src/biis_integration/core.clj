(ns biis-integration.core
  (:use [biis-integration.applied.adjustment]
        [biis-integration.applied.applied :only [get-token]]
        [biis-integration.applied.home :only [get-home-policy]]
        [biis-integration.config :only [home-mta-config]]
        [biis-integration.journey :only [mta-adjust run-journey]]
        [biis-integration.paysafe :only [paysafe-auth]]
        [biis-integration.util :only [delete-directory-recursive log today tomorrow with-context]])
  (:import (java.io File)))

(defn -main []
  (when (.exists (File. "output"))
    (delete-directory-recursive (File. "output")))
  (.mkdir (File. "output"))
  (let [{:keys [policy-codes]} home-mta-config
        {token "access_token"} (get-token {})
        run-mta (fn [policy-code]
                  (-> (run-journey mta-adjust
                                   {:token                         token
                                    :policy-code                   policy-code
                                    :output-folder                 policy-code
                                    :update-cover-details-override {:request
                                                                    {:voluntaryExcessAmount 0
                                                                     :buildingsCoverAmount  350000
                                                                     :contentsCoverAmount   35000
                                                                     :unspecifiedRiskAmount 3500
                                                                     :specifiedItems        []}}
                                    :temp-quote-start              tomorrow})
                      :context
                      (select-keys [:policy-code :quote-amount :amount])
                      log))]
    (pmap run-mta policy-codes)
    (shutdown-agents)))