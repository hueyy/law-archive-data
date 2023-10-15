#!/usr/bin/env bb

(ns input.hearings.get_hearings
  (:require [clojure.math :as math]
            [babashka.curl :as curl]
            [babashka.pods :as pods]
            [cheshire.core :as json]
            [input.utils.general :as utils]
            [input.hearings.populate_hearing_data :refer [populate-hearing-data]])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter])
  (:gen-class))


(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s])

(def URL "https://www.judiciary.gov.sg/hearing-list/GetFilteredList")
(def JSON_FILE "data/hearings.json")

(defn- make-request-body
  ([] (make-request-body 0))
  ([page] {:model {:CurrentPage page
                   :SelectedCourtTab ""
                   :SearchKeywords ""
                   :SearchKeywordsGrouping ""
                   :SelectedCourt ""
                   :SelectedLawFirms []
                   :SelectedJudges []
                   :SelectedHearingTypes []
                   :SelectedStartDate nil
                   :SelectedEndDate nil
                   :SelectedPageSize 500
                   :SelectedSortBy ""}}))

(defn- get-hearing-list-page-raw
  [page]
  (-> (curl/post URL (-> page
                         (make-request-body)
                         (utils/make-json-response-body)))
      :body
      (json/parse-string true)
      :listPartialView
      (utils/parse-html)))

(defn- get-hearing-type [html-el]
  (let [selection (s/select
                   (s/child (s/class "hearing-metadata")
                            (s/class "hearing-type"))
                   html-el)]
    (if (> (count selection) 0)
      (->> selection (first) (utils/get-el-content))
      nil)))

(defn- format-timestamp [timestamp]
  (.format DateTimeFormatter/ISO_LOCAL_DATE_TIME
           (LocalDateTime/parse timestamp
                                (DateTimeFormatter/ofPattern "dd MMM yyyy', 'h:mm a"))))

(defn- parse-hearing-element [html-el]
  (let [hearing-metadata-els
        (s/select (s/child (s/class "hearing-metadata")
                           (s/class "metadata-wrapper")
                           (s/class "metadata"))
                  html-el)]
    {:title (->> html-el :attrs :title)
     :link (->> html-el :attrs :href
                (str "https://www.judiciary.gov.sg"))
     :type (get-hearing-type html-el)
     :reference (if (> (count hearing-metadata-els) 1)
                  (->> hearing-metadata-els
                       (second)
                       (utils/get-el-content))
                  nil)
     :timestamp (->> hearing-metadata-els
                     (first)
                     (utils/get-el-content)
                     (format-timestamp))
     :venue (->> html-el
                 (s/select (s/child (s/class "hearing-item-wrapper")))
                 (first)
                 (s/select (s/child (s/class "text")))
                 (first)
                 (utils/get-el-content))
     :coram (->> html-el
                 (s/select (s/child (s/class "hearing-item-wrapper")))
                 (second)
                 (s/select (s/child (s/class "text")))
                 (first)
                 (utils/get-el-content))}))

(defn- parse-hearing-list-html
  [h-map]
  (->> h-map
       (s/select (s/child (s/and (s/class "list-item")
                                 (s/tag :a))))
       (map parse-hearing-element)))

(defn- get-pagination-status
  "Returns the number of additional pages there are"
  [h-map]
  (let [pagination-status (->> h-map
                               (s/select
                                (s/child (s/class "pagination-summary")))
                               (first)
                               (utils/get-el-content)
                               (re-find #"Showing results 1-500 of (\d{3,})."))]
    (if (nil? pagination-status)
      0
      (-> pagination-status (last) (Integer.)
          (/ 500) (math/ceil) (int)))))

(defn get-hearing-list []
  (let [first-page-html (get-hearing-list-page-raw 0)
        additional-pages-count (get-pagination-status first-page-html)
        additional-pages (map #(get-hearing-list-page-raw (inc %))
                              (range additional-pages-count))]
    (flatten (cons (parse-hearing-list-html first-page-html)
                   (map parse-hearing-list-html additional-pages)))))

(defn -main []
  (->> (get-hearing-list)
       (populate-hearing-data)
       (json/generate-string)
       (spit JSON_FILE)))
