#!/usr/bin/env bb

(require '[clojure.string :as str]
         '[babashka.curl :as curl]
         '[babashka.pods :as pods])

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :as bootleg]
         '[pod.retrogradeorbit.hickory.select :as s])

(defn get-el-content [el]
  (->> el :content (str/join "")))

(defn get-hearing-detail-raw [url]
  (-> (curl/get url)
      :body))

(defn parse-hearing-detail [html]
  (let [el (bootleg/convert-to html :hickory)]
    {:nature-of-case (let [nature-el (->> el
                                          (s/select (s/child (s/class "detail-wrapper")
                                                             (s/class "row")
                                                             (s/class "hearing-item")))
                                          (first))
                           nature-label (->> nature-el
                                             (s/select (s/child (s/class "label")))
                                             (first)
                                             (get-el-content))]
                       (if (= nature-label "Nature of case")
                         (->> nature-el
                              (s/select (s/child (s/class "text")))
                              (first)
                              (get-el-content))
                         nil))
     :hearing-type (->> el
                        (s/select (s/child (s/class "row")
                                           (s/class "hearing-item")
                                           (s/class "text")))
                        (second)
                        (get-el-content))}))

