(ns input.lss.dt-reports
  (:require [babashka.pods :as pods]
            [input.utils.general :as utils]
            [input.utils.xml :as xml]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s]
         '[pod.retrogradeorbit.bootleg.utils :as butils])

(def DOMAIN "https://lawgazette.com.sg")
(def URL (str DOMAIN "/category/notices/disciplinary-tribunal-reports/feed/"))
(def JSON_FILE "data/lss-dt-reports.json")

(defn get-reports-page
  ([] (get-reports-page 1))
  ([page-number]
   (-> (utils/curli URL)
       (xml/parse-rss-feed))))

(defn parse-report-detail-html [h-map]
  (let [article (->> h-map
                     (s/select (s/descendant (s/class "mkdf-content")
                                             (s/tag :article)))
                     (first))]
    {:html (-> article (butils/convert-to :html))}))

(defn parse-report-detail-html [h-map]
  (let [article (->> h-map
                     (s/select (s/descendant (s/tag :article)
                                             (s/class "mkdf-post-text-inner")))
                     (first))
        timestamp (->> h-map
                       (s/select
                        (s/and (s/tag :meta)
                               (s/attr :property #(= % "article:published_time"))))
                       (first)
                       :attrs
                       :content)
        header-els (->> article
                        (s/select (utils/find-in-text #"^In the Matter of")))
        header-tag (->> header-els
                        (first)
                        :tag)]
    {:timestamp timestamp
     :header-tag header-tag}))

(defn get-report-detail [url]
  (-> (utils/curli url)
      (utils/parse-html)))


(def data (-> (slurp "test.xml")
              (xml/parse-rss-feed)))

