(ns biis-integration.mta-adjust-journey-test
  (:require [biis-integration.journey :refer [mta-adjust run-journey]]
            [biis-integration.util :refer [tomorrow]]
            [clojure.test :refer :all]))

(def policies
  {"REEN61002" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  255000
                :contentsCoverAmount   25000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}
   "JOHJ02001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  260000
                :contentsCoverAmount   26000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}
   "REEJ33001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  350000
                :contentsCoverAmount   30000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}
   "MOMO11001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  270000
                :contentsCoverAmount   27000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}
   "MURR06001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  240000
                :contentsCoverAmount   24000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}
   "JOJO14003" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  280000
                :contentsCoverAmount   28000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}
   "JIMJ02001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  290000
                :contentsCoverAmount   29000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}
   "MURM26001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  250000
                :contentsCoverAmount   25000
                :unspecifiedRiskAmount 0
                :specifiedItems        []}

   "REEN62001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  190000
                :contentsCoverAmount   19000
                :unspecifiedRiskAmount 2500
                :specifiedItems        [{:typeOf "Laptop" :description "Laptop" :value 1500}]}
   "REED08001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  250000
                :contentsCoverAmount   25000
                :unspecifiedRiskAmount 3000
                :specifiedItems        [{:typeOf "Jewellery" :description "Diamond Ring" :value 2000}]}
   "JONT08001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  300000
                :contentsCoverAmount   30000
                :unspecifiedRiskAmount 3500
                :specifiedItems        [{:typeOf "Jewellery" :description "Gold Ring" :value 3000}]}
   "JONA01001" {:voluntaryExcessAmount 0
                :buildingsCoverAmount  290000
                :contentsCoverAmount   29000
                :unspecifiedRiskAmount 2500
                :specifiedItems        [{:typeOf "HearingAid" :description "hearing aid" :value 1000}]}})

(defn adjust [prefix policy-code cover-details-update]
  (let [cover (get policies policy-code)]
    (run-journey mta-adjust {:output-folder                 (str prefix "_" policy-code)
                             :policy-code                   policy-code
                             :update-cover-details-override {:request (cover-details-update cover)}
                             :temp-quote-start              tomorrow})))

(deftest no-changes
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "noChanges" policy-code identity)
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 0
    "JOHJ02001" 530.60 0
    "REEJ33001" 604.94 0
    "MOMO11001" 541.46 0
    "MURR06001" 618.99 0
    "JOJO14003" 619.30 0
    "JIMJ02001" 581.68 0
    "MURM26001" 598.70 0

    "REEN62001" 536.56 0
    "REED08001" 615.92 0
    "JONT08001" 666.09 0
    "JONA01001" 629.32 0))

(deftest min-buildings-cover
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "minBuildingsCover" policy-code
                   #(assoc % :buildingsCoverAmount 175000))
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 -86.22
    "JOHJ02001" 530.60 -86.48
    "REEJ33001" 604.94 -157.44
    "MOMO11001" 541.46 -97.16
    "MURR06001" 618.99 -68.5
    "JOJO14003" 619.30 -94.18
    "JIMJ02001" 581.68 -91.72
    "MURM26001" 598.70 -72.42

    "REEN62001" 536.56 0
    "REED08001" 615.92 -77.92
    "JONT08001" 666.09 -102.14
    "JONA01001" 629.32 -89.89))

; maximum allowed is 50% of buildings cover or 250,000
(deftest max-buildings-cover
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "maxBuildingsCover" policy-code
                   #(assoc % :buildingsCoverAmount 1000000))
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 311.97
    "JOHJ02001" 530.60 312.90
    "REEJ33001" 604.94 244.39
    "MOMO11001" 541.46 302.23
    "MURR06001" 618.99 841.05
    "JOJO14003" 619.30 711.42
    "JIMJ02001" 581.68 651.25
    "MURM26001" 598.70 781.21

    "REEN62001" 536.56 470.09
    "REED08001" 615.92 402.24
    "JONT08001" 666.09 632.06
    "JONA01001" 629.32 632.71))

(deftest min-contents-cover
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "minContentsCover" policy-code
                   #(assoc % :contentsCoverAmount 15000))
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 0
    "JOHJ02001" 530.60 0
    "REEJ33001" 604.94 0
    "MOMO11001" 541.46 0
    "MURR06001" 618.99 0
    "JOJO14003" 619.30 0
    "JIMJ02001" 581.68 0
    "MURM26001" 598.70 0

    "REEN62001" 536.56 0
    "REED08001" 615.92 0
    "JONT08001" 666.09 0
    "JONA01001" 629.32 0))

(deftest max-contents-cover
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "maxContentsCover" policy-code
                   (fn [cover] (->> (/ (:buildingsCoverAmount cover) 2)
                                    (assoc cover :contentsCoverAmount))))
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 54.43
    "JOHJ02001" 530.60 53.09
    "REEJ33001" 604.94 76.58
    "MOMO11001" 541.46 53.50
    "MURR06001" 618.99 0
    "JOJO14003" 619.30 0
    "JIMJ02001" 581.68 0
    "MURM26001" 598.70 0

    "REEN62001" 536.56 22.99
    "REED08001" 615.92 50.28
    "JONT08001" 666.09 0
    "JONA01001" 629.32 0))

(deftest max-unspecified
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "maxUnspecifiedCover" policy-code
                   #(assoc % :unspecifiedRiskAmount 10000))
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 119.29
    "JOHJ02001" 530.60 119.29
    "REEJ33001" 604.94 119.29
    "MOMO11001" 541.46 119.29
    "MURR06001" 618.99 72.93
    "JOJO14003" 619.30 72.93
    "JIMJ02001" 581.68 72.93
    "MURM26001" 598.70 75.21

    ;"REEN62001" 536.56 - ;"Total All Risks > 60% Contents Sum Insured"
    "REED08001" 615.92 83.51
    "JONT08001" 666.09 30.54
    "JONA01001" 629.32 34.60))

(deftest empty-specified-cover
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "emptySpecifiedCover" policy-code
                   #(assoc % :specifiedItems []))
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 0
    "JOHJ02001" 530.60 0
    "REEJ33001" 604.94 0
    "MOMO11001" 541.46 0
    "MURR06001" 618.99 0
    "JOJO14003" 619.30 0
    "JIMJ02001" 581.68 0
    "MURM26001" 598.70 0

    "REEN62001" 536.56 -25.21
    "REED08001" 615.92 -24.15
    "JONT08001" 666.09 -40.44
    "JONA01001" 629.32 -28.66))

; < 60% of contents
(deftest full-specified-cover
  (are [policy-code exp-original-amount exp-amount]
    (= [exp-original-amount, exp-amount]
       (-> (adjust "fullSpecifiedCover" policy-code
                   #(assoc % :specifiedItems [{:typeOf "Laptop" :description "Laptop" :value 3000}
                                              {:typeOf "Jewellery" :description "Diamond Ring" :value 1000}
                                              {:typeOf "MusicalInstrument" :description "Guitar" :value 1000}
                                              {:typeOf "Other" :description "Albums" :value 1000}]))
           :context
           (select-keys [:quote-amount :amount])
           vals))
    "REEN61002" 529.24 86.21
    "JOHJ02001" 530.60 86.21
    "REEJ33001" 604.94 86.21
    "MOMO11001" 541.46 86.21
    "MURR06001" 618.99 190.69
    "JOJO14003" 619.30 190.69
    "JIMJ02001" 581.68 190.69
    "MURM26001" 598.70 192.97

    "REEN62001" 536.56 60.99
    "REED08001" 615.92 62.06
    "JONT08001" 666.09 112.11
    "JONA01001" 629.32 123.88))

