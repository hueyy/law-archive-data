(ns input.judgments.utils
  (:require [babashka.pods :as pods]
            [babashka.curl :as curl]
            [clojure.string :as str]
            [input.utils.xml :as xml]
            [clojure.set :refer [rename-keys]]
            [input.utils.date :as date]
            [input.utils.general :as utils]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s]
         '[pod.retrogradeorbit.bootleg.utils :as butils])

(defn get-field-value [table-el field-regex]
  (let [field-value (s/select (s/descendant
                               (s/and (s/class "info-row")
                                      (s/has-child
                                       (s/and (s/class "txt-label")
                                              (utils/find-in-text field-regex))))
                               (s/class "txt-body"))
                              table-el)]
    (if (empty? field-value)
      nil
      (->> field-value
           (first)
           (utils/get-el-content)
           (utils/clean-string)))))

(defn get-field-from-table [html regex]
  (-> (s/select (s/id "info-table") html)
      (first)
      (get-field-value regex)))


(defn parse-counsel-clause [clause]
  ;; TODO: handle instructed counsel, applicants in person, amicus curiae, etc. 
  (let [role-matches (re-find
                      #"(?i) for the (defendant|claimant|plaintiff|appellant|applicant|respondent)s?\.?"
                      clause)
        role-clause (first role-matches)
        role (-> role-matches (second) (str/lower-case))
        remainder (str/replace clause role-clause "")
        law-firm (-> (re-find #".+\((.+)\)$" remainder) (last))
        entity-matches (-> remainder
                           (str/replace (str " (" law-firm ")") "")
                           (str/split #"(, | and )"))]
    {:role role
     :law-firm law-firm
     :counsel entity-matches}))

(defn parse-counsel [counsel-str]
  (->> (str/split counsel-str #";")
       (map utils/clean-string)))

(defn get-case-detail [url]
  (let [html (-> (curl/get url)
                 :body
                 (utils/parse-html))
        body (s/select (s/descendant (s/id "mlContent")
                                     (s/tag :root))
                       html)
        title (->> (s/select (s/descendant (s/class "title")
                                           (s/class "caseTitle"))
                             html)
                   (first)
                   (utils/get-el-content)
                   (utils/clean-string))
        citation (->> (s/select (s/and (s/class "Citation")
                                       (s/class "offhyperlink"))
                                html)
                      (first)
                      (utils/get-el-content)
                      (utils/clean-string))
        date (->> (s/select (s/class "Judg-Hearing-Date")
                            html)
                  (first)
                  (utils/get-el-content)
                  (utils/clean-string)
                  (date/parse-date "d MMMM yyyy")
                  (date/to-iso-8601-date))
        case-number (get-field-from-table html #"(?i)^Case Number$")
        coram (get-field-from-table html #"(?i)^Coram$")
        court (get-field-from-table html #"(?i)^Tribunal/Court$")
        counsel (-> (get-field-from-table html #"(?i)^Counsel Name\(s\)$")
                    (parse-counsel))
        tags (->> (s/select (s/child (s/class "contentsOfFile")
                                     (s/and (s/tag :p)
                                            (s/class "txt-body")))
                            html)
                  (map #(-> % (utils/get-el-content) (utils/clean-string))))]
    {:title title
     :citation citation
     :date date ; date of the judgment
     :case-number case-number
     :coram coram
     :court court
     :counsel counsel
     :tags tags
     :html (butils/convert-to body :html)}))

(defn get-feed [url]
  (->> (curl/get url)
       :body
       (xml/parse-rss-feed)
       :items
       (map #(rename-keys % {:link :url
                             :pub-date :timestamp
                             ; timestamp is the publication date of the RSS item
                             }))))

(defn populate-case-data [feed-item]
  (->> feed-item
       :url
       (get-case-detail)
       (merge feed-item)))