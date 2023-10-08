#!/usr/bin/env bb

 (ns scripts.computed-columns
   (:require [babashka.process :refer [sh]]
             [scripts.utils :as utils]
             [taoensso.timbre :as timbre]
             [clojure.string :as str]
             [cheshire.core :as json]))

(def FINANCIAL_PENALTY_REGEX (re-pattern "(?im)a\\s+financial\\s+penalty\\s+of\\s+S?\\$([0-9,]+)(\\.00)?"))
(defn- get-financial-penalties [pdf-content]
  (let [vals (map #(-> %
                       (second)
                       (str/replace "," "")
                       (Integer/parseInt))
                  (re-seq FINANCIAL_PENALTY_REGEX pdf-content))]
    (if (empty? vals)
      {:sum 0
       :max 0}
      {:sum (reduce + vals)
       :max (apply max vals)})))

(def PDPC_DECISIONS_TABLE "`pdpc_decisions`")
(defn- add-computed-columns-to-pdpc-decisions [db]
  (try
    (utils/run-sql-on-db db (str "ALTER TABLE "
                                 PDPC_DECISIONS_TABLE
                                 " ADD COLUMN `financial_penalties` TEXT;"))
    (catch Exception e
      (timbre/error "`financial_penalties` column already exists")))
  (let [decisions (utils/run-sqlite-utils-on-db db
                                                (str "SELECT _id, `pdf-content` FROM "
                                                     PDPC_DECISIONS_TABLE))]
    (utils/run-sql-on-db db (->> (map #(let [{id :_id
                                              pdf-content :pdf-content} %
                                             financial-penalties (-> pdf-content
                                                                     (get-financial-penalties)
                                                                     (json/encode))]
                                         (str "UPDATE " PDPC_DECISIONS_TABLE
                                              " SET financial_penalties = '"
                                              financial-penalties
                                              "' WHERE _id IS '"
                                              id
                                              "';"))
                                      decisions)
                                 (str/join)))))

(defn add-computed-columns [db]
  (add-computed-columns-to-pdpc-decisions db))
