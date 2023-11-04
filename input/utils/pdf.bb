(ns input.utils.pdf
  (:require [babashka.process :refer [sh shell]]
            [clojure.string :as str]
            [taoensso.timbre :as timbre]))

(defn random-pdf-filename
  [length]
  (str (->> (repeatedly #(rand-nth "abcdefghijklmnopqrstuvwxyz"))
            (take length)
            (apply str))
       ".pdf"))

(defn download [url filename]
  (shell "wget" "--output-document" filename url))

(defn to-text [filename]
  (-> (sh ["pdftotext" filename "-"])
      :out))

(defn run-ocr [filename & {:keys [skip-strategy
                                  optimize
                                  invalidate-signatures?
                                  output-type]
                           :or {skip-strategy :none
                                optimize "0"
                                invalidate-signatures? true
                                output-type :pdf}}]
  (timbre/info "Running ocrmypdf with skip-strategy:" skip-strategy)
  (let [temp-filename (random-pdf-filename 5)]
    (sh "mv" filename temp-filename)
    (apply shell (filter #(-> % (str/blank?) (not))
                         ["poetry" "run"
                          "ocrmypdf"
                          "-l" "eng"
                          "--rotate-pages"
                          "--deskew"
                          "--optimize" optimize
                          (if invalidate-signatures?
                            "--invalidate-digital-signatures"
                            "")
                          "--output-type" (case output-type
                                            :pdf "pdf"
                                            :pdfa "pdfa")
                          (case skip-strategy
                            :skip-text "--skip-text"
                            :redo-ocr "--redo-ocr"
                            :force-ocr "--force-ocr"
                            "")
                          temp-filename
                          filename]))
    (sh "rm" temp-filename)))

(defn get-content-from-url [url & {:keys [ocr?
                                          ocr-options]
                                   :or {ocr? false
                                        ocr-options {}}}]
  (timbre/info "Handling PDF: " url ocr? ocr-options)
  (let [file-name (random-pdf-filename 5)]
    (download url file-name)
    (when ocr?
      (apply run-ocr (concat [file-name]
                             (apply concat ocr-options))))
    (let [pdf-content (to-text file-name)]
      (sh "rm" file-name)
      pdf-content)))
