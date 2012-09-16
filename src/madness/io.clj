(ns madness.io
  "I/O helper routines"

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "GNU General Public License - v3"
              :url "http://www.gnu.org/licenses/gpl.txt"}}

  (:require [madness.config :as cfg]
            [clojure.string :as str]
            [fs.core :as fs]))

(defn write-out-dir
  "Given a filename, write a string into it, creating the file and the
  directories if needed. The destination directory can be overridden
  via the configuration mechanism."

  [file str]

  (let [fn (str/join "/" [(cfg/dirs :output) file])]
    (println "Writing" fn "...")
    (fs/mkdirs (fs/parent fn))
    (spit fn str :encoding "UTF-8")))
