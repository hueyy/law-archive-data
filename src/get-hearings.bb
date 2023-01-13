#!/usr/bin/env bb

(require '[clojure.math :as math]
         '[clojure.string :as str]
         '[babashka.curl :as curl]
         '[babashka.pods :as pods]
         '[cheshire.core :as json])

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :as bootleg]
         '[pod.retrogradeorbit.hickory.select :as s])

(def URL "https://www.judiciary.gov.sg/hearing-list/GetFilteredList")
(defn make-request-body
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

(defn get-el-content [el]
  (->> el :content (str/join "")))

(defn get-hearing-list-page-raw
  [page]
  (str "<html>"
       (-> (curl/post URL {:headers {"Content-Type" "application/json; charset=utf-8"}
                           :body (json/generate-string (make-request-body page))})
           :body
           (json/parse-string true)
           :listPartialView)
       "</html>"))

(defn get-hearing-type [html-el]
  (let [selection (s/select
                   (s/child (s/class "hearing-metadata")
                            (s/class "hearing-type"))
                   html-el)]
    (if (> (count selection) 0)
      (->> selection (first) (get-el-content))
      nil)))

(defn parse-hearing-element [html-el]
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
                       (get-el-content))
                  nil)
     :timestamp (->> hearing-metadata-els
                     (first)
                     (get-el-content))
     :venue (->> html-el
                 (s/select (s/child (s/class "hearing-item-wrapper")))
                 (first)
                 (s/select (s/child (s/class "text")))
                 (first)
                 (get-el-content))
     :coram (->> html-el
                 (s/select (s/child (s/class "hearing-item-wrapper")))
                 (second)
                 (s/select (s/child (s/class "text")))
                 (first)
                 (get-el-content))}))

(defn parse-hearing-list-html
  [html]
  (->> (bootleg/convert-to html :hickory)
       (s/select (s/child (s/and (s/class "list-item")
                                 (s/tag :a))))
       (map parse-hearing-element)))

(defn get-pagination-status
  "Returns the number of additional pages there are"
  [html]
  (let [pagination-status (->> (bootleg/convert-to html :hickory)
                               (s/select
                                (s/child (s/class "pagination-summary")))
                               (first)
                               (get-el-content)
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



(->> (get-hearing-list)
     (json/generate-string)
     (spit "hearings.json"))