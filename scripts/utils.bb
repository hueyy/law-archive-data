(ns scripts.utils
  (:require [babashka.process :refer [process sh shell]]
            [cheshire.core :as json]
            [taoensso.timbre :as timbre]))

(defn run-sql-file-on-db [db f]
  (let [stream (-> (process ["cat" f]) :out)]
    @(process ["sqlite3" db] {:in stream
                              :out :inherit})))

(defn run-sql-on-db [db query]
  (shell "sqlite3" db query))

(defn run-sqlite-utils-on-db
  "Use this when you need the output of the SQL query"
  [db query]
  (timbre/info "Running SQL query: " query)
  (-> (sh "poetry run" "sqlite-utils"
          db query)
      :out
      (json/parse-string true)))

(defmacro try-ignore-error [& body]
  `(try ~@body
        (catch Exception e
          (timbre/error e))))

(defmacro try-ignore-errors [& body]
  `(do
     ~@(map (fn [e]
              `(try
                 ~e
                 (catch Exception e
                   (timbre/error e))))
            body)))