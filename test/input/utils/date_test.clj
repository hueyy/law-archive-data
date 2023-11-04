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
  (testing "Whether short dates are successfully parsed")
  (are [input expected-output]
       (let [[year month day] expected-output]
         (= (date/parse-short-date input)
            (LocalDate/of year month day)))
    "23 Jan 2001" [2001 1 23]
    "8 July 2023" [2023 7 8]
    "1 June 2020" [2020 6 1]))

(deftest parse-rfc-2822-date
  (testing "Whether RFC 2822 dates are successfully parsed")
  (are [input output]
       (let [parsed-date (date/parse-rfc-2822-date input)]
        ;;  (= (and (not (nil? parsed-date))
        ;;          (instance? LocalDateTime parsed-date))
        ;;     output)
         parsed-date)
    "Fri, 03 Nov 2023 16:00:00 GMT" true
    "Sat, 13 Mar 2010 11:29:05 -0800" true))

(deftest to-iso-8601-date
  (testing "Whether dates are successfully formatted in ISO8601 format")
  (are [input expected-output]
       (if (nil? input)
         (= (date/to-iso-8601-date input) expected-output)
         (let [[year month day hour minute second] input
               output-date (LocalDateTime/of year month day
                                             hour minute second)]
           (= (date/to-iso-8601-date output-date) expected-output)))
    [2023 5 2 12 31 57] "2023-05-02"
    nil nil))

(defn -main []
  (fix-month-names)
  (parse-short-date)
  (parse-rfc-2822-date)
  (to-iso-8601-date))