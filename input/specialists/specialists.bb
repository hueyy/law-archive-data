(ns input.specialists.specialists
  (:require [babashka.pods :as pods]
            [babashka.http-client :as http]
            [input.utils.general :as utils]
            [cheshire.core :as json]
            [babashka.curl :as curl]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s]
         '[pod.retrogradeorbit.bootleg.utils :as butils])

(def URL "https://www.sal.org.sg/views/ajax")

(defn fetch-html []
  (-> (http/post URL {:headers {"User-Agent" utils/USER_AGENT
                                "X-Requested-With" "XMLHttpRequest"
                                "Origin" "https://www.sal.org.sg"
                                "Referer" "https://www.sal.org.sg/Services/Appointments/Specialist-Accreditation/Find-a-Specialist"}
                      :form-params {:field_practice_area_target_id "All"
                                    :field_designation_target_id "All"
                                    :items_per_page "10" ; All
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

(defn get-accredited-specialists []
  ())