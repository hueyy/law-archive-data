(ns input.pdpc.decisions
  (:require [babashka.curl :as curl]
            [babashka.pods :as pods]
            [clojure.string :as str]
            [input.utils.general :as utils]
            [input.utils.date :as date]
            [cheshire.core :as json]
            [taoensso.timbre :as timbre]
            [input.utils.pdf :as pdf]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s])

(def DOMAIN "https://www.pdpc.gov.sg")
(def URL (str DOMAIN "/api/pdpcenforcementcases/getenforcementcaselisting"))
(def JSON_FILE "data/pdpc-decisions.json")

(defn- get-decisions-page
  ([] (get-decisions-page 1))
  ([page-number]
   (-> (curl/post URL (-> {:industry "all"
                           :nature "all"
                           :decision "all"
                           :penalty "all"
                           :page page-number}
                          (utils/make-json-response-body)))
       :body
       (json/parse-string true))))

(defn- parse-decision-json [json]
  (merge json
         {:timestamp (-> json :timestamp
                         (date/parse-short-date)
                         (date/to-iso-8601))
          :url (->> json :url
                    (utils/make-absolute-url DOMAIN))}))

(defn- get-all-decisions []
  (let [{total-pages :totalPages
         first-page :items} (get-decisions-page)]
    (timbre/info "Fetched first page of PDPC decisions")
    (->> (reduce (fn [acc cur]
                   (timbre/info "Fetching PDPC decision page: "
                                cur)
                   (concat acc (->> cur
                                    (get-decisions-page)
                                    :items)))
                 first-page
                 (range 2 (+ 1 total-pages)))
         (map parse-decision-json))))

(defn- get-latest-n-decision-pages [n]
  (reduce (fn [acc cur]
            (Thread/sleep 5000)
            (timbre/info "Fetching PDPC decision page: " cur)
            (concat acc (->> cur
                             (get-decisions-page)
                             :items
                             (map parse-decision-json))))
          '()
          (range 1 (+ 1 n))))

(defn parse-tag-html [h-map]
  (->> h-map
       (s/select (s/tag :a))
       (first)
       (utils/get-el-content)
       (utils/clean-string)))

(defn- parse-decision-detail-html [h-map]
  (let [article (->> h-map
                     (s/select (s/and (s/class "detail-content")
                                      (s/tag :article)))
                     (first))
        pdf-url (->> article
                     (s/select (s/tag :a))
                     (first)
                     :attrs
                     :href
                     (utils/make-absolute-url DOMAIN))]
    {:title (->> article
                 (s/select (s/and (s/class "page-title")
                                  (s/tag :h2)))
                 (first)
                 (utils/get-el-content)
                 (utils/clean-string))
     :timestamp (->> article
                     (s/select (s/class "page-date"))
                     (first)
                     (utils/get-el-content)
                     (utils/clean-string)
                     (date/parse-short-date)
                     (date/to-iso-8601))
     :description (-> (->> article
                           (s/select (s/and (s/class "rte")
                                            (s/tag :div)))
                           (first)
                           (utils/get-el-content)
                           (utils/clean-string))
                      (str/replace #"\s*(Click here for more information|Click here to find out more)\.?\s*$" ""))
     :tags (->> article
                (s/select (s/descendant (s/class "taglist")
                                        (s/class "taglist__list")
                                        (s/tag :li)))
                (map parse-tag-html))
     :pdf-url pdf-url
     :pdf-content (pdf/get-content-from-url pdf-url)}))

(defn- get-decision-detail [url]
  (timbre/info "Fetching PDPC decision detail: " url)
  (-> (curl/get url)
      :body
      (utils/parse-html)
      (parse-decision-detail-html)))

(defn -main []
  (->>
   (get-latest-n-decision-pages 3)
   (map #(merge % (get-decision-detail (:url %))))
   (json/generate-string)
   (spit JSON_FILE)))