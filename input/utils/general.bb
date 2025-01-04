(ns input.utils.general
  (:require [clojure.zip :as zip]
            [babashka.fs :as fs]
            [babashka.pods :as pods]
            [clojure.string :as str]
            [cheshire.core :as json]
            [babashka.process :refer [sh shell]]
            [taoensso.timbre :as timbre]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :as bootleg])

(defn parse-html
  "takes HTML and returns hickory map"
  [html]
  (-> (str "<html>" html "</html>")
      (bootleg/convert-to :hickory)))

(defn get-el-content
  ([current output]
   (if (string? current)
     (str output current)
     (if (nil? (:content current))
       output
       (get-el-content
        (str/join "" (map #(get-el-content % output)
                          (:content current)))
        output))))
  ([el]
   (get-el-content el "")))

; Taken from https://github.com/clj-commons/hickory/blob/d721c9accd74b1618200347a0a1f05907441cbfd/src/cljc/hickory/select.cljc#L283
(defn find-in-text
  "Returns a function that takes a zip-loc argument and returns the zip-loc
   passed in if it has some text node in its contents that matches the regular
   expression. Note that this only applies to the direct text content of a node;
   nodes which have the given text in one of their child nodes will not be
   selected."
  [re]
  (fn [hzip-loc]
    (some #(re-find re %) (->> (zip/node hzip-loc)
                               :content
                               (filter string?)))))

(defn normalise-whitespace [input-str]
  (str/replace input-str #"[ \s]" " "))

(defn clean-string [input-str]
  (-> input-str
      (normalise-whitespace)
      (str/trim)))

(defn make-absolute-url [domain url]
  (if (str/starts-with? url "/")
    (str domain url)
    url))

(defn make-json-response-body [body]
  {:headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string body)})

(defn download-binary [url & {:keys [untar? filename]
                              :or {untar? false
                                   filename nil}}]
  (when (or (and untar? (nil? filename))
            (and (not untar?) (-> filename (nil?) (not))))
    (timbre/error "If untar is true, filename must be specified"))
  (let [downloaded-filename (-> url
                                (str/split #"\/")
                                (last))
        extract? (and untar? (-> filename (nil?) (not)))]
    (timbre/info "Downloading from: " url)
    (shell "wget" url)
    (when extract?
      (let [directory-name (-> downloaded-filename
                               (str/split #"\.tar\.gz")
                               (first))]
        (timbre/info "Untarring...")
        (shell "tar --one-top-level -xvf" downloaded-filename)
        (timbre/info "Moving binary out of directory: "
                     (str directory-name "/" filename))
        (shell "mv" (str directory-name "/" filename) ".")
        (timbre/info "Removing directory and archive: "
                     downloaded-filename directory-name)
        (shell "rm -Rf" downloaded-filename directory-name)))
    (timbre/info "Setting execution permissions")
    (shell "chmod +x" (if extract?
                        filename
                        downloaded-filename))))

(def CURL_IMPERSONATE_BINARY "./curl-impersonate-chrome")
(defn download-curl-impersonator []
  (download-binary "https://github.com/lexiforest/curl-impersonate/releases/download/v0.8.2/curl-impersonate-v0.8.2.x86_64-linux-gnu.tar.gz"
                   :untar? true
                   :filename CURL_IMPERSONATE_BINARY))

(def USER_AGENT "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.3")
(defn curli [url & {:keys [raw-args
                           user-agent]
                    :or {raw-args []
                         user-agent USER_AGENT}}]
  (when (-> CURL_IMPERSONATE_BINARY (fs/exists?) (not))
    (download-curl-impersonator))
  (let [command (concat [CURL_IMPERSONATE_BINARY
                         "--user-agent" user-agent]
                        raw-args
                        [url])]
    (-> (sh command)
        :out)))
(defn curli-post-json [url body]
  (-> url
      (curli :raw-args ["--data" (json/generate-string body)
                        "--request" "POST"
                        "--header" "Content-Type: application/json"])
      (json/parse-string
       true)))

(defn random-number [min max]
  (let [range (+ max (- min))]
    (+ min (rand-int range))))

(defn wait-for [min max]
  (Thread/sleep (random-number min max)))