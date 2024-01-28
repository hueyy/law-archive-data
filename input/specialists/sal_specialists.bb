(ns input.specialists.sal-specialists
  (:require [babashka.pods :as pods]
            [babashka.http-client :as http]
            [input.utils.general :as utils]
            [cheshire.core :as json]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s])

(def URL "https://www.sal.org.sg/views/ajax")
(def JSON_FILE "data/sal-specialists.json")

(defn fetch-html []
  (-> (http/post URL {:headers {"User-Agent" utils/USER_AGENT
                                "X-Requested-With" "XMLHttpRequest"
                                "Origin" "https://www.sal.org.sg"
                                "Referer" "https://www.sal.org.sg/Services/Appointments/Specialist-Accreditation/Find-a-Specialist"}
                      :form-params {:field_practice_area_target_id "All"
                                    :field_designation_target_id "All"
                                    :items_per_page "All"
                                    :view_name "find_a_specialist"
                                    :view_display_id "block_1"
                                    :view_args ""
                                    :view_path "/node/545"
                                    :view_base_path ""}})
      :body
      (json/parse-string true)
      (last)
      :data
      (utils/parse-html)))

(defn get-column-contents [row headers & {:keys [get-href?]
                                          :or {get-href? false}}]
  (let [el (->> row
                (s/select (s/attr "headers" #(= % headers)))
                (first))]
    (if get-href?
      (->> el
           (s/select (s/tag :a))
           (first)
           :attrs
           :href)
      (->> el
           (utils/get-el-content)
           (utils/clean-string)))))

(defn parse-row [row]
  {:name (get-column-contents row "view-title-table-column")
   :organisation (get-column-contents row "view-field-firm-table-column")
   :practice-area (get-column-contents row "view-field-practice-area-table-column")
   :designation (get-column-contents row "view-field-designation-table-column")
   :year-of-accreditation (get-column-contents row "view-field-year-of-accreditation-table-column")
   :url (get-column-contents row "view-title-table-column" :get-href? true)})

(defn get-accredited-specialists []
  (map parse-row (->> (fetch-html)
                      (s/select (s/descendant (s/tag :table)
                                              (s/tag :tbody)
                                              (s/tag :tr))))))


(defn -main []
  (->> (get-accredited-specialists)
       (json/generate-string)
       (spit JSON_FILE)))