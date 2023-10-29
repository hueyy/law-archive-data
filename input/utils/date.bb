(ns input.utils.date
  (:require [clojure.string :as str])
  (:import [java.time LocalDate LocalDateTime]
           [java.time.format DateTimeFormatter])
  (:gen-class))

(defn fix-month-names
  "Replaces non-standard month names in short dates, e.g. Sept instead of Sep, June instead of Jun, etc"
  [input-date]
  (-> input-date
      (str/replace #"\bSept\b" "Sep")
      (str/replace #"\bJune\b" "Jun")
      (str/replace #"\bJuly\b" "Jul")))

(defn parse-date
  [pattern date]
  (if (nil? (re-find #"[HmsZ]" "EEE, dd MMM yyyy HH:mm:ss Z"))
    (LocalDate/parse date (DateTimeFormatter/ofPattern pattern))
    (LocalDateTime/parse date (DateTimeFormatter/ofPattern pattern))))

(defn parse-short-date
  "Parses short dates (e.g. 8 Jul 2023)"
  [date]
  (-> date
      (fix-month-names)
      (parse-date "d MMM yyyy")))

(defn parse-long-date
  "Parses dates in the format EEE, dd MMM yyyy HH:mm:ss Z"
  [date]
  (parse-date "EEE, dd MMM yyyy HH:mm:ss Z" date))

(defn to-iso-8601
  "Format a date in ISO 8601 format"
  [date]
  (if (str/blank? date)
    nil
    (.format DateTimeFormatter/ISO_LOCAL_DATE date)))
