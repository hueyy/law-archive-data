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
  ([namespace input-file id]
   (-> (str "poetry run git-history file"
            " " DB_FILE
            " " input-file
            " --namespace " "'" namespace "'"
            (if (nil? id) "" (str " --id " id)))
       (#(try
           (shell %)
           (catch Exception e
             (timbre/error e)))))))

(def HEARINGS_JSON "data/hearings.json")
(def SC_JSON "data/sc.json")
(def PDPC_UNDERTAKINGS_JSON "data/pdpc-undertakings.json")
(def PDPC_DECISIONS_JSON "data/pdpc-decisions.json")
(def LSS_DT_REPORTS_JSON "data/lss-dt-reports.json")

(defn run []
  (generate-db "hearings" HEARINGS_JSON "link")
  (generate-db "sc" SC_JSON "name")
  (generate-db "pdpc_undertakings" PDPC_UNDERTAKINGS_JSON "url")
  (generate-db "pdpc_decisions" PDPC_DECISIONS_JSON "url")
  (generate-db "lss_dt_reports" LSS_DT_REPORTS_JSON)
  (utils/run-sql-file-on-db DB_FILE "scripts/create-views.sql")
  (add-computed-columns DB_FILE))

(run)