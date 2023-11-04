(ns input.utils.date
  (:require [clojure.string :as str])
  (:import [java.time LocalDate LocalDateTime ZonedDateTime]
           [java.time.format DateTimeFormatter])
  (:gen-class))

(defn fix-month-names
  "Replaces non-standard month names in short dates, e.g. Sept instead of Sep, June instead of Jun, etc"
  [input-date]
  (if (not (str/blank? input-date))
    (-> input-date
        (str/replace #"\bSept\b" "Sep")
        (str/replace #"\bJune\b" "Jun")
        (str/replace #"\bJuly\b" "Jul"))
    input-date))

(defn parse-date
  [pattern date]
  (when (not (str/blank? date))
    (cond
      (re-find #"[zZ]" pattern)
      (ZonedDateTime/parse date (DateTimeFormatter/ofPattern pattern))
      (re-find #"[Hms]" pattern)
      (LocalDateTime/parse date (DateTimeFormatter/ofPattern pattern))
      :else
      (LocalDate/parse date (DateTimeFormatter/ofPattern pattern)))))

(defn parse-short-date
  "Parses short dates (e.g. 8 Jul 2023)"
  [date]
  (->> date
       (fix-month-names)
       (parse-date "d MMM yyyy")))

(defn parse-rfc-2822-date
  "Parses dates in the RFC 2822 format. See: https://www.rfc-editor.org/rfc/rfc2822#section-3.3"
  [date]
  (when (not (str/blank? date))
    (->> (for [format ["EEE, dd MMM yyyy HH:mm:ss Z"
                       "EEE, dd MMM yyyy HH:mm:ss zzz"
                       "dd MMM yyyy HH:mm:ss Z"
                       "dd MMM yyyy HH:mm:ss zzz"]]
           (try
             (parse-date format date)
             (catch Exception _)))
         (filter #(not (nil? %)))
         (first))))

(defn to-iso-8601
  "Format a date in ISO 8601 (full) format"
  [date]
  (when (not (nil? date))
    (.format DateTimeFormatter/ISO_DATE_TIME date)))

(defn to-iso-8601-date
  "Format a date in ISO 8601 (date only) format"
  [date]
  (when (not (nil? date))
    (.format DateTimeFormatter/ISO_LOCAL_DATE date)))
