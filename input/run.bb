#!/usr/bin/env bb

(ns input.run
  (:require [cheshire.core :as json]
            [input.get_hearings :refer [get-hearing-list]]
            [input.populate_hearing_data :refer [populate-hearing-data]]))

(->> (get-hearing-list)
     (populate-hearing-data)
     (json/generate-string)
     (spit "hearings.json"))