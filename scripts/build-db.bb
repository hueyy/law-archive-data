#!/usr/bin/env bb

(require '[babashka.process :refer [shell]])

(defn generate-db []
  (shell "poetry run" "git-history" "file" "hearings.db" "hearings.json"
         "--start-at" "96398149e899fe720a936dbcd6864f4b4c99b340"))

;; (defn add-views []
;;   (let [stream (-> (process '["cat" "create-views.sql"]) :out)]
;;     @(process ["sqlite3" "hearings.db"] {:in stream
;;                                          :out :inherit})))

(defn run []
  (generate-db)
  ;; (add-views)
  )

(run)