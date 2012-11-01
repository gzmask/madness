(ns madness.io
  "I/O helper routines"

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "GNU General Public License - v3"
              :url "http://www.gnu.org/licenses/gpl.txt"}}

  (:require [madness.config :as cfg]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as h]
            [fs.core :as fs]
            [clj-yaml.core :as y])
  (:import (java.io StringReader)
           (org.pegdown PegDownProcessor)))

(defn find-files
  "List all HTML and Markdown files within a given directory. Returns
   an array of java.io.File objects."
  
  [dir]

  (sort #(compare %2 %1) (fs/find-files dir #".*\.(html|md|markdown|mdwn)$")))

(defn write-out-dir
  "Given a filename, write a string into it, creating the file and the
  directories if needed. The destination directory can be overridden
  via the configuration mechanism."

  [file str]

  (let [fn (str/join "/" [(cfg/dirs :output) file])]
    (println "Writing" fn "...")
    (fs/mkdirs (fs/parent fn))
    (spit fn str :encoding "UTF-8")))

(defmulti preprocess-file
  "Given a file type (a file-name extension) and a file, transform it
  into a format that can be fed to Enlive, and conforms to the
  requirements set by the rendering engine."

  (fn [type file] type))

(defn- split-metadata-and-content
  "Split metadata (things between \"---\" lines at the top of the
  file) out of the file contents.

  Returns a vector containing the metadata and the rest of the content."

  [content]

  (let [idx (.indexOf content "---" 4)]
    [(.substring content 4 idx) (.substring content (+ 3 idx))]))

(defn- split-summary-from-content
  "Split the short summary and the body of a post into two. The end of
  the short summary must be marked with <!-- more -->.

  Returns a vector containing the short summary and the main contents."

  [content]

  (let [idx (.indexOf content "<!-- more -->")]
    (if (= idx -1)
      ["" content]
      [(.substring content 0 idx) (.substring content (+ 13 idx))])))

(defn- html-wrap
  "Wrap a value within a HTML element."

  [e v]

  (str "<" e ">" v "</" e ">"))

(defn- v->html
  "Convert a key-value pairs data part into a HTML tag string. If the
  key is :tags, then the value shall be a list of <tag>
  elements. Otherwise the value is left as-is."

  [k, v]

  (if (= k :tags)
    (reduce (fn [h v] (str h (html-wrap "tag" v))) "" v)
    v))

(defn- yaml->html-string
  "Convert YAML-format meta-data to HTML string."

  [metadata]

  (let [meta (y/parse-string metadata)]
    (reduce (fn [h [k v]]
              (str h (html-wrap (name k) (v->html k v))))
            "" meta)))

;; Processing markdown files has three steps:
;;
;; * Split the metadata from the content, and parse the former as if it was YAML.
;; * Split the short summary from the main data
;; * Assemble these three into a HTML format that the processing engine expects.
(defmethod preprocess-file ".md" [_ file]
  (let [[metadata content] (split-metadata-and-content (slurp file))
        [summary content] (split-summary-from-content content)]
    (StringReader. (str (html-wrap "article" (yaml->html-string metadata))
                        (html-wrap "summary" (.markdownToHtml (PegDownProcessor.) summary))
                        (html-wrap "section" (.markdownToHtml (PegDownProcessor.) content))))))

(defmethod preprocess-file ".mdwn" [_ file]
  (preprocess-file ".md" file))

(defmethod preprocess-file ".markdown" [_ file]
  (preprocess-file ".md" file))

;; Anything that is not markdown, is let through as-is, and we'll
;; assume it is HTML.
(defmethod preprocess-file :default [_ file]
  file)

(defn read-file
  "Load and process a file. Based on the extension, the file will
  either be processed as raw HTML, or preprocessed as Markdown first."

  [file]

  (let [content (preprocess-file (-> file .getName fs/extension)
                                 file)]
    (h/html-resource content)))
