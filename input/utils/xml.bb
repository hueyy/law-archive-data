(ns input.utils.xml
  (:require [clojure.data.xml :as xml]
            [clojure.string :as str]
            [input.utils.date :as date]))

(defn is-xml-el? [el]
  (instance? clojure.data.xml.node.Element el))

(defn filter-by-tag [tag els]
  (filter #(= (:tag %) tag) els))

(defn get-element-content-by-tag [tag els]
  (->> els
       (filter-by-tag tag)
       (first)
       :content
       (str/join "")))

(defn parse-rss-item [xml-item]
  (let [elements (->> xml-item :content
                      (filter is-xml-el?))]
    {:title (get-element-content-by-tag :title elements)
     :link (get-element-content-by-tag :link elements)
     :pub-date (->> (get-element-content-by-tag :pubDate elements)
                    (date/parse-long-date)
                    (date/to-iso-8601))}))

(defn parse-rss-feed [content]
  (let [elements (->> content
                      (xml/parse-str)
                      :content
                      (filter is-xml-el?)
                      (first)
                      :content
                      (filter is-xml-el?))]
    {:title (get-element-content-by-tag :title elements)
     :description (get-element-content-by-tag :description elements)
     :items (->> elements
                 (filter-by-tag :item)
                 (map parse-rss-item))}))