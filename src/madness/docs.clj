(ns madness.docs
  "Helper functions to generate documentation from the
   madness sources.

   One's expected to use this namespace via lein run, such as:
    `lein run -m madness.docs/generate-docs`"

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "GNU General Public License - v3"
              :url "http://www.gnu.org/licenses/gpl.txt"}}

  (:use [marginalia.core]))

(def docs #^{:private true}
  ["../madness", "core", "config", "resources", "render", "io", "utils", "blog",
   "blog/nav", "blog/recent", "blog/post", "blog/page",
   "blog/archive", "blog/index", "blog/atom"])

(defn- fiddle-path
  [path]
  (str "src/madness/" path ".clj"))

(defn generate-docs
  "Generate API docs from the madness sources. This function assumes
  the sources are available as in the git repository."
  []

  (binding [marginalia.html/*resources* ""]
    (run-marginalia
     (into ["-f" "index.html"]
           (map fiddle-path docs)))))
