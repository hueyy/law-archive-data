#!/usr/bin/env bb

(ns input.populate_hearing_data
  (:require [clojure.zip :as zip]
            [clojure.string :as str]
            [babashka.curl :as curl]
            [babashka.pods :as pods]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :as bootleg]
         '[pod.retrogradeorbit.hickory.select :as s])

(defn get-el-content [el] (->> el :content (str/join "")))

; Taken from https://github.com/clj-commons/hickory/blob/d721c9accd74b1618200347a0a1f05907441cbfd/src/cljc/hickory/select.cljc#L283
(defn find-in-text
  "Returns a function that takes a zip-loc argument and returns the zip-loc
   passed in if it has some text node in its contents that matches the regular
   expression. Note that this only applies to the direct text content of a node;
   nodes which have the given text in one of their child nodes will not be
   selected."
  [re]
  (fn [hzip-loc]
    (some #(re-find re %) (->> (zip/node hzip-loc)
                               :content
                               (filter string?)))))

(defn get-hearing-detail-raw [url] (-> (curl/get url) :body))

(defn get-field-value [parent-el field-regex]
  (let [field-value (s/select (s/child
                               (s/and (s/class "hearing-item")
                                      (s/has-child
                                       (s/and (s/class "label")
                                              (find-in-text field-regex))))
                               (s/class "text"))
                              parent-el)]
    (if (empty? field-value)
      nil
      (->> field-value (first) (get-el-content)))))

(defn parse-party-el [party-el]
  (let [fields (s/select (s/child (s/class "hearing-item")) party-el)]
    {:role (->> fields (first)
                (s/select (s/class "label")) (first)
                (get-el-content))
     :name (->> fields (first)
                (s/select (s/class "text")) (first)
                (get-el-content))
     :representation (if (> (count fields) 1)
                       (->> fields (last)
                            (s/select (s/class "text")) (first)
                            (get-el-content))
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
    {:nature-of-case (get-field-value hearing-details-el #"^Nature of case$")
     :hearing-type (get-field-value hearing-details-el #"^Hearing type$")
     :charge-number (get-field-value hearing-details-el #"^Charge number$")
     :offence-description (get-field-value hearing-details-el #"^Offence description$")
     :hearing-outcome (get-field-value hearing-details-el #"^Hearing outcome$")
     :parties (map parse-party-el parties-els)}))



(defn populate-hearing-data [hearings]
  (map #(try
          (Thread/sleep 5000)
          (->> % :link
               (get-hearing-detail-raw)
               (parse-hearing-detail)
               (merge %))
          (catch Exception e
            (println (str "Caught exception: "
                          (.getMessage e)))
            %))
       hearings))
