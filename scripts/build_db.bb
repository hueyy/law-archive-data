#!/usr/bin/env bb

(ns scripts.build-db
  (:require [babashka.process :refer [shell]]
            [taoensso.timbre :as timbre]
            [scripts.utils :as utils]
            [scripts.computed-columns :refer [add-computed-columns]]))

(def DB_FILE "data/data.db")

;; (defn get-ignored-commits [file]
;;   (when (-> "mergestat" (fs/exists?) (not))
;;     (utils/download-binary "https://github.com/mergestat/mergestat-lite/releases/download/v0.6.1/mergestat-linux-amd64.tar.gz"
;;                            :untar? true
;;                            :filename "mergestat"))
;;   (->> (-> (sh "./mergestat -f json"
;;                (str "SELECT files.path, hash FROM commits, files WHERE files.path IS NOT '" file "'"))
;;            :out
;;            (json/parse-string true))
;;        (map :hash)
;;        (str/join " ")))

(defn generate-db
  ([namespace input-file]
   (generate-db namespace input-file nil))
  ([namespace input-file & {:keys [id
                                   start-at
                                   convert]
                            :or {id nil
                                 start-at nil
                                 convert nil}}]
   (-> (str "poetry run git-history file"
            " " DB_FILE
            " " input-file
            " --namespace " "'" namespace "'"
            (if (nil? id) "" (str " --id " id))
            (if (nil? start-at) "" (str "--start-at " start-at))
            (if (nil? convert) "" (str "--convert " convert)))
       (#(try
           (shell %)
           (catch Exception e
             (timbre/error e)))))))

(defn setup-fts [table fields]
  (try
    (apply shell (concat ["poetry" "run"
                          "sqlite-utils" "enable-fts"
                          DB_FILE table]
                         fields))
    (catch Exception e
      (timbre/error e))))

(def HEARINGS_JSON "data/hearings.json")
(def SC_JSON "data/sc.json")
(def PDPC_UNDERTAKINGS_JSON "data/pdpc-undertakings.json")
(def PDPC_DECISIONS_JSON "data/pdpc-decisions.json")
(def LSS_DT_REPORTS_JSON "data/lss-dt-reports.json")

(defn run []
  (generate-db "hearings" HEARINGS_JSON
               :id "link")
  (generate-db "sc" SC_JSON
               :id "name")
  (generate-db "pdpc_undertakings" PDPC_UNDERTAKINGS_JSON
               :id "url")
  (generate-db "pdpc_decisions" PDPC_DECISIONS_JSON
               :id "url")
  (generate-db "lss_dt_reports" LSS_DT_REPORTS_JSON
               :id "unique_id"
               :start-at "8f629c3927889da7257bf73ac3cbf35f96cc954e"
               :convert "[{**item, 'unique_id': item['title']+'_'+item['url']} for item in json.loads(content)]")
  (utils/run-sql-file-on-db DB_FILE "scripts/create-views.sql")
  (add-computed-columns DB_FILE)
  (setup-fts "hearings"
             ["title" "parties" "offence-description"])
  (setup-fts "pdpc_decisions"
             ["title" "description" "pdf-content"])
  (setup-fts "pdpc_undertakings"
             ["organisation" "description" "pdf-content"])
  (setup-fts "lss_dt_reports"
             ["title" "content" "pdf-content"]))

(run)