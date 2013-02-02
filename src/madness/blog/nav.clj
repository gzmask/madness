(ns madness.blog.nav
  "## Building blocks for navigation

  All HTML pages of the generated site will contain some common
  navigation items: a global list of recent items, and the list of all
  available tags.

  The functions herein implement the recent item & tag lists handling."

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012-2013 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "Creative Commons Attribution-ShareAlike 3.0"
              :url "http://creativecommons.org/licenses/by-sa/3.0/"}}
  
  (:require [net.cgrand.enlive-html :as h]
            [clojure.string :as s]
            [madness.config :as cfg]
            [madness.utils :as utils]))

;; The template must have a part with the id of
;; `#madness-recent-posts`, under which must be an unordered list
;; (with only one item in the template). The single item of that list
;; will be used as the template snippet for displaying recent items.
;;
;; The list item itself, must contain an anchor, whose `href` and
;; content this snippet will change to the URL and title of the post,
;; respectively.
(h/defsnippet recent-item (cfg/template) [:#madness-recent-posts :li]
  [title url]

  [:a] (utils/rewrite-link url title))

;; Similarly to recent items, the template must also contain a
;; `#madness-tags` item, also with an unordered list beneath it, where
;; the single element of the template must also have an anchor.
;;
;; The anchor's `href` and textual content will be rewritten by this
;; snippet to the URL for the tag archive, and the name of the tag
;; itself.
(h/defsnippet tag-item (cfg/template) [:#madness-tags :li]
  [tag]

  [:a] (utils/rewrite-link (utils/tag-to-url tag) tag))

;; To render all recent posts, we simply clone the snippet above for
;; every recent post we should display. How many are displayed, is
;; controlled by the configuration.
(defn recent-posts
  [all-posts]

  (h/clone-for [post (take (dec (cfg/recent-posts :total)) all-posts)]
               (h/substitute (recent-item (:title post) (:url post)))))

;; And to display all tags, we also clone the `tag-item` snippet above
;; for each and every unique tag.
(defn all-tags
  [all-posts]

  (h/clone-for [tag (sort #(compare (s/lower-case %1)
                                    (s/lower-case %2))
                          (utils/tags all-posts))]
               (h/substitute (tag-item tag))))

;; These last two functions - `recent-posts` and `all-tags` - will be
;; used by the various page rendering templates in [blog.archive][1],
;; [blog.post][2], and [blog.page][3].
;;
;; [1]: #madness.blog.archive
;; [2]: #madness.blog.post
;; [3]: #madness.blog.page
