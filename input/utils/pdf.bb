(ns input.utils.pdf
  (:require [babashka.process :refer [sh]]
            [taoensso.timbre :as timbre]))

(def PDF_FILENAME "temp.pdf")

(defn download [url filename]
  (sh "wget" "--output-document" filename url))

(defn to-text [filename]
  (-> (sh ["pdftotext" filename "-"])
      :out))

(defn run-ocr [filename]
  (timbre/info "Running ocrmypdf")
  (sh "ocrmypdf -l eng --rotate-pages --deskew --skip-text" filename))

(defn get-content-from-url [url & {:keys [ocr?]
                                   :or {ocr? false}}]
  (timbre/info "Handling PDF: " url)
  (download url PDF_FILENAME)
  (when ocr?
    (run-ocr PDF_FILENAME))
  (let [pdf-content (to-text PDF_FILENAME)]
    (sh "rm" PDF_FILENAME)
    pdf-content))
