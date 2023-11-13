(ns scripts.build-frontend-assets
  (:require [babashka.process :refer [shell]]
            [taoensso.timbre :as timbre]))

(comment (defn download-file [url & {:keys [filename]
                              :or {filename nil}}]
  (timbre/info "Downloading from: " url)
  (shell (str "wget "
              (if (nil? filename)
                ""
                (str "--output-document " filename " "))
              url)))
