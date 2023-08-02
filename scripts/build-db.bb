#!/usr/bin/env bb

(require '[babashka.process :refer [sh process]]
         '[babashka.fs :as fs]
         '[cheshire.core :as json]
         '[clojure.string :as str])

(def MERGESTAT_BINARY "mergestat")
(def DB_FILE "hearings.db")

(def FRONTEND_DIR "./frontend/public/db")
(def FRONTEND_DB_FILE (str FRONTEND_DIR DB_FILE "."))
(def FRONTEND_CONFIG_FILE (str FRONTEND_DIR "config.json"))

(def SERVER_CHUNK_SIZE (* 10 1024 1024)) ; 10 MiB
(def SUFFIX_LENGTH 3)

(defn download-mergestat []
  (sh "wget" "https://github.com/mergestat/mergestat-lite/releases/download/v0.6.1/mergestat-linux-amd64.tar.gz")
  (sh "tar -xvf" "mergestat-linux-amd64.tar.gz")
  (sh "rm" "mergestat-linux-amd64.tar.gz" "libmergestat.so")
  (sh "chmod +x" MERGESTAT_BINARY))

(defn get-ignored-commits []
  (when (not (fs/exists? MERGESTAT_BINARY))
    (download-mergestat))
  (->> (-> (sh "mergestat -f json" "SELECT hash FROM commits WHERE committer_email IS NOT 'actions@users.noreply.github.com'")
           :out
           (json/parse-string true))
       (map :hash)
       (str/join " ")))

(defn generate-db []
  (sh "poetry run" "git-history" "file" DB_FILE "hearings.json"
      "--start-at" "96398149e899fe720a936dbcd6864f4b4c99b340"
      "--skip" (get-ignored-commits)))

(defn optimise-db []
  (let [stream (-> (process '["cat" "./sql/optimise.sql"]) :out)]
    @(process ["sqlite3" DB_FILE] {:in stream
                                   :out :inherit})))

(defn regenerate-db []
  (sh "rm" (str FRONTEND_DB_FILE "*"))
  (sh "split" DB_FILE
      (str "--bytes=" SERVER_CHUNK_SIZE)
      FRONTEND_DB_FILE
      (str "--suffix-length=" SUFFIX_LENGTH)
      "--numeric-suffixes")
  (let [bytes (-> (sh "stat --printf='%s'" DB_FILE) :out)
        request-chunk-size (->
                            (sh "sqlite3" DB_FILE "pragma page_size")
                            :out
                            (str/trim))]
    (->> {:serverMode "chunked"
          :requestChunkSize request-chunk-size
          :databaseLengthBytes bytes
          :serverChunkSize SERVER_CHUNK_SIZE
          :urlPrefix "hearings.db."
          :suffixLength SUFFIX_LENGTH}
         (json/generate-string)
         (spit FRONTEND_CONFIG_FILE))))

;; (defn add-views []
;;   (let [stream (-> (process '["cat" "./sql/create-views.sql"]) :out)]
;;     @(process ["sqlite3" "hearings.db"] {:in stream
;;                                          :out :inherit})))

(defn run []
  (generate-db)
  (optimise-db)
  (regenerate-db)
  ;; (add-views)
  )

(run)