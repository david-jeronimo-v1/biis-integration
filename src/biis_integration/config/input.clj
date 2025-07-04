(ns biis-integration.config.input
  (:require [biis-integration.journey :refer [mta-adjust-and-accept]]))

(def update-cover-details {:request
                              {:voluntaryExcessAmount 0
                               :buildingsCoverAmount  400000
                               :contentsCoverAmount   200000
                               :unspecifiedRiskAmount 10000
                               :specifiedItems        [{:typeOf "Jewellery" :description "Diamond Ring" :value 5000}
                                                       {:typeOf "Jewellery" :description "Gold Ring" :value 5000}]}})

(def input {:journey  mta-adjust-and-accept
            :log-keys [:policy-code :quote-amount :amount]
            :payloads [{:policy-code                   "JONJ10001"
                        :output-folder                 "JONJ10001"
                        :output-format                 :json
                        :update-cover-details-override update-cover-details
                        :temp-quote-start              "2025-08-02T00:00:00"}]})