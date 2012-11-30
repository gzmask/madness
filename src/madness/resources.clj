(ns madness.resources
  "## Madness resources

  Pretty much for convenience only, and to not reload all the posts
  and pages every time we need them, this namespace contains a few
  convenience Vars, with pages and posts preloaded.

  These are heavily used by madness.render."

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "Creative Commons Attribution-ShareAlike 3.0"
              :url "http://creativecommons.org/licenses/by-sa/3.0/"}}

  (:require [madness.blog :as blog]
            [madness.utils :as utils]))

;; Convenience Var containing all the blog posts
(defonce posts (blog/load-posts))
;; Convenience Var contiaining all the blog pages
(defonce pages (blog/load-pages))

;; Convenience Var containing all the blog posts, grouped by tags.
(defonce posts-tag-grouped (utils/group-blog-by-tags posts))
