#!/usr/bin/env bb

(ns scripts.build-db
  (:require [babashka.process :refer [shell sh]]
            [babashka.fs :as fs]
            [cheshire.core :as json]
            [clojure.string :as str]
            [taoensso.timbre :as timbre]
            [scripts.utils :as utils]
            [scripts.computed-columns :refer [add-computed-columns]]))

(def MERGESTAT_BINARY "mergestat")
(def DB_FILE "data/data.db")

(defn download-mergestat []
  (timbre/info "Downloading mergestat")
  (sh "wget" "https://github.com/mergestat/mergestat-lite/releases/download/v0.6.1/mergestat-linux-amd64.tar.gz")
  (timbre/info "Extracting mergestat")
  (sh "tar -xvf" "mergestat-linux-amd64.tar.gz")
  (timbre/info "Cleaning up unnecessary mergestat files")
  (sh "rm" "mergestat-linux-amd64.tar.gz" "libmergestat.so")
  (timbre/info "Setting permissions for mergestat")
  (sh "chmod +x" MERGESTAT_BINARY))

(defn get-ignored-commits [file]
  (when (not (fs/exists? MERGESTAT_BINARY))
    (download-mergestat))
  (->> (-> (sh "./mergestat -f json"
               (str "SELECT files.path, hash FROM commits, files WHERE files.path IS NOT '" file "'"))
           :out
           (json/parse-string true))
       (map :hash)
       (str/join " ")))

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

(defn run []
  (generate-db "hearings" HEARINGS_JSON "link")
  (generate-db "sc" SC_JSON "name")
  (generate-db "pdpc_undertakings" PDPC_UNDERTAKINGS_JSON "url")
  (generate-db "pdpc_decisions" PDPC_DECISIONS_JSON "url")
  (utils/run-sql-file-on-db DB_FILE "scripts/create-views.sql")
  (add-computed-columns DB_FILE))

(run)