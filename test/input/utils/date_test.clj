(ns test.input.utils.date-test
  (:require [clojure.test :refer [deftest testing are]]
            [input.utils.date :as date]))

(deftest fix-month-names
  (testing "Whether non-standard short month names are normalised to standard short month names")
  (are [input expected-output]
       (= (date/fix-month-names input) expected-output)
    "5 Sept 1998" "5 Sep 1998"
    "23 Jan 2001" "23 Jan 2001"
    "9th October 2050" "9th October 2050"
    "30 June 2005" "30 Jun 2005"
    "17th July 2020" "17th Jul 2020"
    nil nil
    "" ""))

(defn -main []
  (fix-month-names))