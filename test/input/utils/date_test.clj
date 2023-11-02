(ns test.input.utils.date-test
  (:require [clojure.test :refer [deftest testing are]]
            [input.utils.date :as date])
  (:import [java.time LocalDate LocalDateTime])
  (:gen-class))

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

(deftest parse-short-date
  (testing "Whether short dates are successfully passed as dates")
  (are [input expected-output]
       (let [[year month day] expected-output]
         (= (date/parse-short-date input)
            (LocalDate/of year month day)))
    "8 July 2023" [2023 7 8]
    "1 June 2020" [2020 6 1]
    "31 December 1990" [1990 12 31]))

(deftest to-iso-8601
  (testing "Whether dates are successfully formatted in ISO8601 format")
  (are [input expected-output]
       (let [[year month day hour minute second] input]
         (= (date/to-iso-8601 (LocalDateTime/of year month day
                                                hour minute second))
            expected-output))

    [2023 5 2 12 31 57] "2023-05-02"
    nil nil))

(defn -main []
  (fix-month-names))