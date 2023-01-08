#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json]
         '[pod.retrogradeorbit.bootleg.utils :as bootleg]
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


(defn get-hearing-list-raw
  "Returns HTML"
  [] (str "<html>"
          (-> (curl/post URL {:headers {"Content-Type" "application/json; charset=utf-8"}
                              :body (json/generate-string (make-request-body))
                              :debug true})
              :body
              (json/parse-string true)
              :listPartialView)
          "</html>"))

(defn parse-hearing-element [html-el]
  (let []
    {:title (->> html-el :attrs :title)
     :link (->> html-el :attrs :href (str "https://www.judiciary.gov.sg"))}))

(defn parse-hearing-list-html
  [html]
  (->> (bootleg/convert-to html :hickory)
       (s/select (s/child (s/and (s/class "list-item")
                                 (s/tag :a))))
       (take 5)
       (map parse-hearing-element)))

;; (spit "test.html" (get-hearing-list-raw))
(->> (slurp "test.html")
     (parse-hearing-list-html))