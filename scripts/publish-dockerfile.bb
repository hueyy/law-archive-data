#!/usr/bin/env bb

(require '[babashka.process :as process])

(def DOCKER_TAG "hueyy/sg-courts-hearings-list-datasette:latest")

(defn build-docker []
  (process/shell "docker" "build" "." "-t" DOCKER_TAG))

(defn push-docker []
  (process/shell "docker" "push" DOCKER_TAG))

(defn run []
  (build-docker)
  (push-docker))

(run)