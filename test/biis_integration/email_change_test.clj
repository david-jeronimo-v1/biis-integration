(ns biis-integration.email-change-test
  (:require [biis-integration.journey :refer [run-journey update-email]]
            [biis-integration.util :refer [log today tomorrow]]
            [clojure.test :refer :all]))

(deftest no-email-change
  (testing "No email change"
    (let [result (run-journey
                   update-email
                   {:output-folder                   "test-no-email-change"
                    :create-quote-override           {"home_quote"
                                                      {"cover_details"         {"start_date" today}
                                                       "policy_holder_details" {"email" "tilibi8770@buides.com"}}}
                    :update-contact-details-override {:request {:email "tilibi8770@buides.com"}}
                    :temp-quote-start                tomorrow})
          {:keys [context]} result
          {:keys [updated-email]} context]

      (is (= "tilibi8770@buides.com" updated-email))

      (log (select-keys context [:policy-code :amount :updated-email])))))

(deftest email-change
  (testing "Email change"
    (let [result (run-journey
                   update-email
                   {:output-folder                   "test-email-change"
                    :create-quote-override           {"home_quote"
                                                      {"cover_details"         {"start_date" today}
                                                       "policy_holder_details" {"email" "tilibi8770@buides.com"}}}
                    :update-contact-details-override {:request {:email "david.jeronimo@version1.com"}}
                    :temp-quote-start                tomorrow})
          {:keys [context]} result
          {:keys [updated-email]} context]

      (is (= "david.jeronimo@version1.com" updated-email))

      (log (select-keys context [:policy-code :amount :updated-email])))))