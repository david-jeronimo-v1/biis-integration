(ns biis-integration.config.input
  (:require [biis-integration.journey :refer [mta-adjust]]
            [biis-integration.util :refer [tomorrow]]))

(def update-cover-details-25 {:request
                              {:voluntaryExcessAmount 0
                               :buildingsCoverAmount  250000
                               :contentsCoverAmount   25000
                               :unspecifiedRiskAmount 2500
                               :specifiedItems        []}})

(def update-cover-details-35 {:request
                              {:voluntaryExcessAmount 0
                               :buildingsCoverAmount  350000
                               :contentsCoverAmount   35000
                               :unspecifiedRiskAmount 3500
                               :specifiedItems        []}})

(def input {:journey  mta-adjust
            :log-keys [:policy-code :quote-amount :amount]
            :payloads [{:policy-code                   "SMIJ23001"
                        :output-folder                 "SMIJ23001"
                        :update-cover-details-override update-cover-details-25
                        :temp-quote-start              tomorrow}
                       {:policy-code                   "JOMA02001"
                        :output-folder                 "JOMA02001"
                        :update-cover-details-override update-cover-details-25
                        :temp-quote-start              tomorrow}
                       {:policy-code                   "RICR05001"
                        :output-folder                 "RICR05001"
                        :update-cover-details-override update-cover-details-35
                        :temp-quote-start              tomorrow}
                       {:policy-code                   "RIVA01001"
                        :output-folder                 "RIVA01001"
                        :update-cover-details-override update-cover-details-35
                        :temp-quote-start              tomorrow}]})