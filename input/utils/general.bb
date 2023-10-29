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

(defn download-curl-impersonator []
  (download-binary "https://github.com/lwthiker/curl-impersonate/releases/download/v0.5.4/curl-impersonate-v0.5.4.x86_64-linux-gnu.tar.gz"
                   :untar? true
                   :filename "curl-impersonate-ff"))

(def CURL_IMPERSONATE_BINARY "curl-impersonate-ff")
(def USER_AGENT "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
(defn curli [url]
  (when (-> CURL_IMPERSONATE_BINARY (fs/exists?) (not))
    (download-curl-impersonator))
  (-> (sh CURL_IMPERSONATE_BINARY
          "--user-agent" USER_AGENT
          url)
      :out))

