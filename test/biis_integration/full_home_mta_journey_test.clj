(ns biis-integration.full-home-mta-journey-test
  (:require [biis-integration.journey :refer [full-mta-journey run-journey]]
            [biis-integration.util :refer [log today tomorrow]]
            [clojure.test :refer :all]))

(deftest increase-unspecified
  (testing "increasing unspecified amount > 500"
    (let [result (run-journey
                   full-mta-journey
                   {:output-folder                 "test-increase-unspecified"
                    :create-quote-override         {"home_quote"
                                                    {"cover_details"
                                                     {"excess_amount"                     0
                                                      "buildings_amount"                  175000
                                                      "contents_amount"                   15000
                                                      "unspecified_personal_items_amount" 0
                                                      "specified_items"                   []
                                                      "start_date"                        today}}}
                    :update-cover-details-override {:request
                                                    {:voluntaryExcessAmount 0
                                                     :buildingsCoverAmount  175000
                                                     :contentsCoverAmount   15000
                                                     :unspecifiedRiskAmount 3000
                                                     :homeOfficeCoverAmount nil
                                                     :smallCraftCoverAmount nil
                                                     :specifiedItems        []}}
                    :temp-quote-start              tomorrow})
          {:keys [context error-message error]} result
          {:keys [quote-amount amount]} context]
      (is (= 500 (-> error (get "status"))))
      (is (= "acceptAdjustment 500" error-message))
      (is (= 379.97 quote-amount))
      (is (> amount 0))

      (log (select-keys context [:policy-code :quote-amount :amount])))))

(deftest add-specified
  (testing "add specified item > 500"
    (let [result (run-journey
                   full-mta-journey
                   {:output-folder                 "test-add-specified"
                    :create-quote-override         {"home_quote"
                                                    {"cover_details"
                                                     {"excess_amount"                     0
                                                      "buildings_amount"                  175000
                                                      "contents_amount"                   15000
                                                      "unspecified_personal_items_amount" 0
                                                      "specified_items"                   []
                                                      "start_date"                        today}}}
                    :update-cover-details-override {:request
                                                    {:voluntaryExcessAmount 0
                                                     :buildingsCoverAmount  175000
                                                     :contentsCoverAmount   15000
                                                     :unspecifiedRiskAmount 0
                                                     :homeOfficeCoverAmount nil
                                                     :smallCraftCoverAmount nil
                                                     :specifiedItems        [{:typeOf "Laptop" :description "Laptop" :value 3000}]}}
                    :temp-quote-start              tomorrow})
          {:keys [context error-message error]} result
          {:keys [quote-amount amount]} context]
      (is (= 500 (-> error (get "status"))))
      (is (= "acceptAdjustment 500" error-message))
      (is (= 379.97 quote-amount))
      (is (> amount 0))

      (log (select-keys context [:policy-code :quote-amount :amount])))))

(deftest no-changes
  (testing "no changes > zero cost"
    (let [result (run-journey
                   full-mta-journey
                   {:output-folder                 "test-no-changes"
                    :create-quote-override         {"home_quote"
                                                    {"cover_details"
                                                     {"excess_amount"                     0
                                                      "buildings_amount"                  175000
                                                      "contents_amount"                   15000
                                                      "unspecified_personal_items_amount" 0
                                                      "specified_items"                   []
                                                      "start_date"                        today}}}
                    :update-cover-details-override {:request
                                                    {:voluntaryExcessAmount 0
                                                     :buildingsCoverAmount  175000
                                                     :contentsCoverAmount   15000
                                                     :unspecifiedRiskAmount 0
                                                     :homeOfficeCoverAmount nil
                                                     :smallCraftCoverAmount nil
                                                     :specifiedItems        []}}
                    :temp-quote-start              tomorrow})
          {:keys [context error]} result
          {:keys [quote-amount amount]} context]

      (is (nil? error))
      (is (= 379.97 quote-amount))
      (is (zero? amount))

      (log (select-keys context [:policy-code :quote-amount :amount])))))

(deftest remove-specified-items
  (testing "remove specified items > zero cost"
    (let [result (run-journey
                   full-mta-journey
                   {:output-folder                 "test-remove-specified-items"
                    :create-quote-override         {"home_quote"
                                                    {"cover_details"
                                                     {"excess_amount"                     0
                                                      "buildings_amount"                  130000
                                                      "contents_amount"                   15000
                                                      "unspecified_personal_items_amount" 0
                                                      "specified_items"                   [{:item "LAPTOP" :value 3000 :description "Laptop"}]
                                                      "start_date"                        today}}}
                    :update-cover-details-override {:request
                                                    {:voluntaryExcessAmount 0
                                                     :buildingsCoverAmount  130000
                                                     :contentsCoverAmount   15000
                                                     :unspecifiedRiskAmount 0
                                                     :homeOfficeCoverAmount nil
                                                     :smallCraftCoverAmount nil
                                                     :specifiedItems        []}}
                    :temp-quote-start              tomorrow})
          {:keys [context error]} result
          {:keys [quote-amount amount]} context]

      (is (nil? error))
      (is (< 370 quote-amount 440))
      (is (< amount 0))

      (log (select-keys context [:policy-code :quote-amount :amount])))))