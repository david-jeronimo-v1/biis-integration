(ns biis-links.check-external-links-test
  (:require [clojure.test :refer :all]
            [biis-links.check-external-links :refer [check-links]]))

(deftest no-email-change
  (testing "No email change"
    (check-links "../boi-ui/packages/boi/src/constants/externalLinks.js")))