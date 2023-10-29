#!/usr/bin/env bb

(ns input.sc.run
  (:require [babashka.curl :as curl]
            [babashka.pods :as pods]
            [clojure.string :as str]
            [input.utils.general :as utils]
            [input.utils.date :as date]
            [cheshire.core :as json]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s])

(def URL "https://www.sal.org.sg/Services/Appointments/Senior-Counsel/Directory")
(def JSON_FILE "data/sc.json")

(def titles ["Mr" "Ms" "Mrs"
             "Professor" "Dr"
             "Attorney-General" "Deputy Attorney-General" "Solicitor-General"
             "Chief Justice" "Judicial Commissioner" "Justice of the Court of Appeal" "Judge of the Appellate Division" "Justice"
             "Deputy Presiding Judge" "Senior Judge" "Judge" "District Judge"])

(def name-regex (re-pattern (str "(" (str/join "|" titles) ") "
                                 "([aA-zZ /-]+)"
                                 "(, Judge of Appeal)?"
                                 "( \\(honoris causa\\))?"
                                 "(\\*)?$")))

(defn- get-page []
  (-> (curl/get URL)
      :body
      (utils/parse-html)))

(defn- parse-name
  "returns map with various keys"
  [raw-name]
  (let [matches (re-matches name-regex raw-name)]
    {:title (second matches)
     :name (nth matches 2)
     :judge-of-appeal (-> (nth matches 3) nil? not)
     :honoris-causa (-> (nth matches 4) nil? not)
     :deceased (-> (nth matches 5) nil? not)}))

(defn- get-span-content
  "gets the text content of the innermost <span>"
  [td]
  (-> (->> td
           (s/select (s/descendant (s/or
                                    (s/tag :span)
                                    (s/tag :font))))
           (#(if (empty? %) td (last %)))
           (utils/get-el-content))
      (utils/clean-string)))

(defn- parse-row [row]
  (let [tds (s/select (s/child (s/tag :td)) row)
        parsed-name (->> tds (second)
                         (get-span-content)
                         (parse-name))]
    (merge parsed-name
           {:organisation (-> (nth tds 2)
                              (get-span-content)
                              (utils/clean-string)
                              (#(if-not (str/blank? %) % nil)))
            :appointment-provision (-> (nth tds 3)
                                       (get-span-content)
                                       (utils/clean-string))
            :appointment-date (-> (nth tds 4)
                                  (get-span-content)
                                  (utils/clean-string)
                                  (date/parse-short-date)
                                  (date/to-iso-8601))})))

(defn- parse-page [h-map]
  (->> (s/select (s/descendant (s/tag :table)
                               (s/tag :tbody)
                               (s/tag :tr))
                 h-map)
       (drop 1)
       (drop-last 6)
       (map parse-row)))

(defn get-data []
  (-> (get-page)
      (parse-page)))

(defn -main []
  (->> (get-data)
       (json/generate-string)
       (spit JSON_FILE)))
