#!/usr/bin/env bb

(require '[babashka.process :refer [shell process]])

(defn generate-db []
  (shell "poetry run" "git-history" "file" "hearings.db" "hearings.json"
         "--id" "link"
         "--start-at" "5ef32d3061fffe1f6a40703ba6cbdcee5166a89d"))

(defn add-views []
  (let [stream (-> (process '["cat" "create-views.sql"]) :out)]
    @(process ["sqlite3" "hearings.db"] {:in stream
                                         :out :inherit})))

(defn run []
  (generate-db)
  (add-views))

(run)