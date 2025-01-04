#!/usr/bin/env bb

(ns input.hearings.populate-hearing-data
  (:require [babashka.pods :as pods]
            [input.utils.general :as utils]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :as bootleg]
         '[pod.retrogradeorbit.hickory.select :as s])

(defn get-hearing-detail-raw [url] (utils/curli url))

(defn get-field-value [parent-el field-regex]
  (let [field-value (s/select (s/child
                               (s/and (s/class "hearing-item")
                                      (s/has-child
                                       (s/and (s/class "label")
                                              (utils/find-in-text field-regex))))
                               (s/class "text"))
                              parent-el)]
    (if (empty? field-value)
      nil
      (->> field-value
           (first)
           (utils/get-el-content)))))

(defn parse-party-el [party-el]
  (let [fields (s/select (s/child (s/class "hearing-item")) party-el)]
    {:role (->> fields (first)
                (s/select (s/class "label")) (first)
                (utils/get-el-content))
     :name (->> fields (first)
                (s/select (s/class "text")) (first)
                (utils/get-el-content))
     :representation (if (> (count fields) 1)
                       (->> fields (last)
                            (s/select (s/class "text")) (first)
                            (utils/get-el-content))
                       nil)}))

(defn parse-hearing-detail [html]
  (let [el (bootleg/convert-to html :hickory)
        hearing-details-el (->> el
                                (s/select (s/child
                                           (s/class "detail-wrapper")
                                           (s/and (s/class "row")
                                                  (s/not
                                                   (s/class "hearing-party")))))
                                first)
        parties-els (->> el
                         (s/select (s/child
                                    (s/and (s/class "row")
                                           (s/class "hearing-party")))))]
    {:nature-of-case (get-field-value hearing-details-el #"(?i)^Nature of case$")
     :hearing-type (get-field-value hearing-details-el #"(?i)^Hearing type$")
     :charge-number (get-field-value hearing-details-el #"(?i)^Charge number$")
     :offence-description (get-field-value hearing-details-el #"(?i)^Offence description$")
     :hearing-outcome (get-field-value hearing-details-el #"(?i)^Hearing outcome$")
     :parties (map parse-party-el parties-els)}))

(defn populate-hearing-data [hearings]
  (map #(try
          (utils/wait-for 2000 8000)
          (->> % :link
               (get-hearing-detail-raw)
               (parse-hearing-detail)
               (merge %))
          (catch Exception e
            (println (str "Caught exception: "
                          (.getMessage e)))
            %))
       hearings))
