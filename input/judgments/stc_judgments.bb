(ns input.judgments.stc-judgments
  (:require [babashka.pods :as pods]
            [babashka.curl :as curl]
            [input.utils.general :as utils]
            [input.utils.xml :as xml]
            [clojure.string :as str]
            [clojure.set :refer [rename-keys]]
            [input.utils.pdf :as pdf]
            [cheshire.core :as json]
            [taoensso.timbre :as timbre]
            [pod.retrogradeorbit.bootleg.utils :as butils]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.hickory.select :as s]
         '[pod.retrogradeorbit.bootleg.utils :as butils])

(def DOMAIN "https://www.lawnet.sg")
(def MAX 1000)
(def URL (str DOMAIN "/lawnet/web/lawnet/free-resources?p_p_id=freeresources_WAR_lawnet3baseportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_resource_id=subordinateRSS&p_p_cacheability=cacheLevelPage&p_p_col_id=column-1&p_p_col_pos=2&p_p_col_count=3&_freeresources_WAR_lawnet3baseportlet_total=" MAX))

(defn get-case-detail [url]
  (let [html (-> (curl/get url)
                 :body
                 (utils/parse-html))
        body (s/select (s/descendant (s/id "mlContent")
                                     (s/tag :root))
                       html)
        citation (s/select (s/and (s/class "Citation")
                                  (s/class "offhyperlink")))]
    {:html (butils/convert-to body :html)}))

(defn get-feed []
  (-> (curl/get URL)
      :body
      (xml/parse-rss-feed)
      :items
      (rename-keys {:link :url
                    :pub-date :timestamp})))

(defn get-stc-judgments [])