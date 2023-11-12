#!/usr/bin/env bb

(ns scripts.dev-docker
  (:require [babashka.process :refer [shell]]))

(def DOCKER_CONFIG "./docker/dev.docker-compose.yml")

(defn -main []
  (shell (str "docker compose --file " DOCKER_CONFIG
              " build"))
  (shell (str "docker compose --file " DOCKER_CONFIG " up")))

(-main)