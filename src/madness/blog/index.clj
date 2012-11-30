(ns madness.blog.index
  "## Rendering the index page

  The index is the root document of the generated site, it has a
  featured - the most recent - blog post, and a list of recent items,
  but no archived ones."

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "Creative Commons Attribution-ShareAlike 3.0"
              :url "http://creativecommons.org/licenses/by-sa/3.0/"}}
  
  (:require [net.cgrand.enlive-html :as h]
            [madness.blog :as blog]
            [madness.blog.nav :as blog-nav]
            [madness.utils :as utils]
            [madness.config :as cfg]
            [madness.blog.recent :as blog-recent]
            [clojure.string :as str]))

;; The featured article has a "Read on" button, which is the
;; `#hero-full` element. It should be, or should have an `a` child,
;; whose `href` this snippet sill replace, and remove the id.
(h/defsnippet index-read-on (cfg/template) [:#hero-full]
  [post]

  [:#hero-full :a] (h/set-attr :href (:url post))
  [:#hero-full] (h/remove-attr :id))

;; The featured article also have a title, the `h1` child of the
;; `hero-unit` element. The `h1` must have an `a` child, whose `href`
;; and textual content will be replaced by the featured article's URL
;; and title, respectively.
(h/defsnippet blog-index-first-title (cfg/template) [:.hero-unit :h1]
  [post]

  [:h1] (h/set-attr :title (:title post))
  [:h1 :a] (utils/rewrite-link (:url post) (:title post)))

;; ### Putting it all together
;;
;; The index page has a featured article, in the `hero-unit` element,
;; a set of recent posts in the `#recents` element, generated using
;; the tools provided by [blog.recent][1].
;;
;; There are no neighbours, no archive, no comments, but the sidebar
;; is, of course, filled out, using the functions provided by
;; [blog.nav][2].
;;
;; [1]: #madness.blog.recent
;; [2]: #madness.blog.nav
;;
(h/deftemplate blog-index (cfg/template)
  [blog-posts _]

  [:.hero-unit] (h/do->
                 (h/content (blog-index-first-title (first blog-posts))
                            (:summary (first blog-posts))
                            (index-read-on (first blog-posts))))
  [:#recents]
    (h/clone-for [rows (utils/blog->table
                        (cfg/recent-posts :columns)
                        (cfg/recent-posts :rows) (rest blog-posts))]
                 (h/do->
                  (h/substitute (blog-recent/recent-post-row rows false))
                  (h/before utils/hr-desktop)))
  [:#post-neighbours] nil
  [:#archive] nil
  [:#disqus] nil
  [:#nav-recent-posts :ul :li] (blog-nav/recent-posts blog-posts)
  [:#nav-tags :ul :li] (blog-nav/all-tags blog-posts))
