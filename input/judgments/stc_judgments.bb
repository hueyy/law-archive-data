(ns input.judgments.stc-judgments
  (:require   [cheshire.core :as json]
              [input.utils.general :as utils]
              [input.judgments.utils :refer [get-feed populate-case-data]]))

(def DOMAIN "https://www.lawnet.sg")
(def MAX 1000)
(def URL (str DOMAIN "/lawnet/web/lawnet/free-resources?p_p_id=freeresources_WAR_lawnet3baseportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=subordinateRSS&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=2&p_p_col_count=3&_freeresources_WAR_lawnet3baseportlet_total=" MAX))
(def JSON_FILE "data/stc-judgments.json")

(defn get-stc-judgments []
  (->> (get-feed URL)
       (map #(do
               (utils/wait-for 2000 8000)
               (populate-case-data %)))))

(defn -main []
  (->> (get-stc-judgments)
       (json/generate-string)
       (spit JSON_FILE)))