(ns input.lss.dt-reports
  (:require [babashka.pods :as pods]
            [input.utils.general :as utils]
            [input.utils.xml :as xml]
            [clojure.string :as str]
            [input.utils.pdf :as pdf]
            [cheshire.core :as json]
            [taoensso.timbre :as timbre]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s]
         '[pod.retrogradeorbit.bootleg.utils :as butils])

(def DOMAIN "https://lawgazette.com.sg")
(def URL (str DOMAIN "/category/notices/disciplinary-tribunal-reports/feed/"))
(def JSON_FILE "data/lss-dt-reports.json")

(def case-title-selector (s/and
                          (utils/find-in-text #"(?i)^In the matter of")
                          (s/not (s/class "mkdf-post-title"))))

(defn is-case-title? [el]
  (->> el
       (s/select case-title-selector)
       (first)
       (nil?)
       (not)))

(defn split-article-by-cases [article-h-map]
  (->> article-h-map
       (s/select (s/class "mkdf-post-text-main"))
       (first)
       :content
       (drop-while #(-> % (is-case-title?) (not)))
       (partition-by is-case-title?)
       (partition 2)
       (map flatten)))

(defn parse-case [raw-case]
  (let [pdf-el (->> {:content raw-case}
                    (s/select (s/descendant (utils/find-in-text #"(?i)access the full report")
                                            (s/tag :a)))
                    (first))]
    (merge
     {:title (->> raw-case
                  (first)
                  (utils/get-el-content)
                  (utils/clean-string))
      :html (->> raw-case
                 (map #(butils/convert-to % :html))
                 (str/join))
      :content (->> raw-case
                    (map #(-> % (utils/get-el-content)
                              (utils/clean-string)))
                    (str/join "\n"))}
     (if (nil? pdf-el)
       {}
       (let [pdf-link (->> pdf-el
                           :attrs
                           :href
                           (utils/make-absolute-url DOMAIN))]
         {:pdf-link pdf-link
          :pdf-content (pdf/get-content-from-url
                        pdf-link
                        :ocr true)})))))

(defn parse-report-detail [h-map]
  (let [article (->> h-map
                     (s/select (s/descendant (s/class "mkdf-content")
                                             (s/tag :article)
                                             (s/class "mkdf-post-text-inner")))
                     (first))
        timestamp (->> h-map
                       (s/select
                        (s/and (s/tag :meta)
                               (s/attr :property #(= % "article:published_time"))))
                       (first)
                       :attrs
                       :content)
        raw-cases (split-article-by-cases article)]
    (map #(-> %
              (parse-case)
              (merge {:timestamp timestamp}))
         raw-cases)))

(def get-report-detail
  (memoize (fn [url]
             (-> (utils/curli url)
                 (utils/parse-html)
                 (parse-report-detail)))))

(defn get-reports-page
  ([] (get-reports-page 1))
  ([page-number]
   (let [url (str URL "?paged=" page-number)]
     (timbre/info "Parsing: " url)
     (-> (utils/curli url)
         (xml/parse-rss-feed)))))

(defn get-all-pages
  ([]
   (get-all-pages 1))
  ([page-number]
   (let [current-page (get-reports-page page-number)
         not-found? (->> current-page
                         :title
                         (re-find #"^Page not found")
                         (first)
                         (nil?)
                         (not))]
     (if not-found?
       (->> [current-page]
            (drop 1)
            (reverse))
       (conj (get-all-pages (inc page-number)) current-page)))))

(defn- get-all-reports []
  (reduce (fn [acc cur]
            (Thread/sleep 5000)
            (timbre/info "Fetching DT report page: " cur)
            (concat acc (get-report-detail cur)))
          []
          (->> (get-all-pages)
               (map :items)
               (flatten)
               (map :link))))

(defn -main []
  (->> (get-all-reports)
       (json/generate-string)
       (spit JSON_FILE)))