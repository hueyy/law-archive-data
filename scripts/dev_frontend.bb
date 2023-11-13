(ns scripts.dev-frontend
  (:require [babashka.process :refer [shell]]
            [taoensso.timbre :as timbre]))

(comment (defn download-file [url & {:keys [filename]
                              :or {filename nil}}]
  (timbre/info "Downloading from: " url)
  (shell (str "wget "
              (if (nil? filename)
                ""
                (str "--output-document " filename " "))
              url))))

(defn run-tailwind []
  (shell "pnpm exec tailwindcss -i ./styles/main.css -o ./dist/main.css --watch"))

(defn -main []
  (shell "pnpm i")
  (run-tailwind))
