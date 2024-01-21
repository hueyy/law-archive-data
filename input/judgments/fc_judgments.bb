(ns input.judgments.fc-judgments
  (:require [cheshire.core :as json]
            [input.judgments.utils :refer [get-feed populate-case-data]]
            [input.utils.general :as utils]))

(def DOMAIN "https://www.lawnet.sg")
(def MAX 1000)
(def URL (str DOMAIN "/lawnet/web/lawnet/free-resources?p_p_id=freeresources_WAR_lawnet3baseportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=juvenileRSS&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=2&p_p_col_count=3&_freeresources_WAR_lawnet3baseportlet_total=" MAX))
(def JSON_FILE "data/fc-judgments.json")

(defn get-fc-judgments []
  (->> (get-feed URL)
       (map #(do
               (utils/wait-for 3000 7000)
               (populate-case-data %)))))

(defn -main []
  (->> (get-fc-judgments)
       (json/generate-string)
       (spit JSON_FILE)))