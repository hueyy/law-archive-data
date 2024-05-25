#!/usr/bin/env bb

(ns input.telco.fbo
  (:require [babashka.curl :as curl]
            [babashka.pods :as pods]
            [input.utils.general :as utils]
            [cheshire.core :as json]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s])

(def URL "https://www.imda.gov.sg/regulations-and-licences/licensing/list-of-telecommunication-and-postal-service-licensees/list-of-facilities-based-operators")
(def JSON_FILE "data/telco-fbo.json")

(defn- get-page []
  (-> (curl/get URL)
      :body
      (utils/parse-html)))

(defn- parse-item [anchor]
  {:name (get (->> anchor
                   (utils/get-el-content)
                   (re-find #"(?i)(.+) \([0-9\.]{1,6}(MB|KB)\)$")) 1)
   :licence-pdf (->> anchor
                     :attrs :href
                     (str "https://www.imda.gov.sg/regulations-and-licences/licensing/list-of-telecommunication-and-postal-service-licensees/"))})

(defn- parse-page [h-map]
  (->> h-map
       (s/select (s/descendant (s/and (s/tag :article)
                                      (s/class "detail-content"))
                               (s/and (s/tag :div)
                                      (s/class "accordion-list__wrap"))
                               (s/and (s/tag :div)
                                      (s/class "accordion-item"))
                               (s/class "accordion-content")
                               (s/tag :ul)
                               (s/tag :li)
                               (s/tag :a)))
       (map parse-item)))

(defn get-data []
  (-> (get-page)
      (parse-page)))

(defn -main []
  (->> (get-data)
       (json/generate-string)
       (spit JSON_FILE)))
