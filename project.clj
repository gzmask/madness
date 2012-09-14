(defproject madness "0.0.0-SNAPSHOT"
  :description "Static site generator, based on [Enlive][1] and
  [Bootstrap][2].

  [1]: https://github.com/cgrand/enlive/wiki
  [2]: http://twitter.github.com/bootstrap/"
  :url "https://github.com/algernon/madness"
  :license {:name "GNU General Public License - v3"
            :url "http://www.gnu.org/licenses/gpl.txt"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [enlive/enlive "1.0.1" :exclusions [org.clojure/clojure]]
                 [clj-time "0.4.4" :exclusions [org.clojure/clojure]]
                 [fs "1.3.2"]]
  :profiles {:dev {:dependencies [[marginalia "0.7.0"]]}}
  :aliases {"build-docs" ["with-profile" "dev" "run" "-m" "madness.docs/generate-docs"]
            "madness" ["run" "-m" "madness.core"]})
