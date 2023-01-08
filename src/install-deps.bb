#!/usr/bin/env bb

(require '[babashka.process :as process])
(require '[babashka.fs :as fs])

(def ARCHIVE_FILENAME "bootleg-0.1.9-linux-amd64.tgz")
(def BINARY_FILENAME "bootleg")

(defn download-archive []
  (process/shell "wget" (str "https://github.com/retrogradeorbit/bootleg/releases/download/v0.1.9/" ARCHIVE_FILENAME)))

(defn setup
  "Setup dependencies"
  [] (do (when (not (fs/exists? ARCHIVE_FILENAME))
           (download-archive))
         (when (not (fs/exists? BINARY_FILENAME))
           (process/shell "tar xvf" ARCHIVE_FILENAME))
         (when (not (fs/executable? BINARY_FILENAME))
           (process/shell "chmod +x" BINARY_FILENAME))))

(setup)