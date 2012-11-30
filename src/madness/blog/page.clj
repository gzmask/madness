(ns madness.blog.page
  "## Loading & rendering of individual static pages

  For the sake of ease, static pages are fully loaded first, and
  turned into a structure that is easy to work with. This namespace
  implements the low-level loading, restructuring and rendering of
  individual static pages.

  Static pages are under `resources/pages` by default, and their path
  below that will be reused as-is for the URL of the generated
  page. That is, `resources/pages/foo/bar/index.html` becomes
  `/foo/bar/index.html`."

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "Creative Commons Attribution-ShareAlike 3.0"
              :url "http://creativecommons.org/licenses/by-sa/3.0/"}}
  
  (:require [net.cgrand.enlive-html :as h]
            [madness.blog.nav :as blog-nav]
            [madness.utils :as utils]
            [madness.config :as cfg]
            [madness.io :as io]
            [clojure.string :as str]
            [clj-time.format :as time-format]))

(defn- page-url
  "Given a file path, strips the pages directory, and returns the
  result, which will be used as the URL for a given page."
  [path]

  (second (first (re-seq (re-pattern (str ".*" (cfg/dirs :pages) "(.*)"))
                         path))))

(defn read-page
  "Read a static page from a while, and restructure it into a
  representation that is easy to work with.

  Each static page must have an `article` element, where the only
  required child is the `title`. It can also - optionally - have a
  `comments` property, which, when set, will enable commenting on the
  particular page.

  Apart from the `article` element, only a `section` element is
  required, whose contents will be used as the main content of the
  static page.

  The structure this function generates, should be pretty clear by
  glancing over the code here."
  
  [file]

  (let [page (io/read-file file)]
    {:title (apply h/text (h/select page [:article :title])),
     :url (page-url (.getPath file))
     :comments (or
                (-> (first (h/select page [:article])) :attrs :comments utils/enabled?)
                (-> (h/text (first (h/select page [:article :comments]))) utils/enabled?)),
     :content (h/select page [:section])}))

;; ### Static page templates

;; The first thing about a page, is its header, the `h1` element of
;; the `hero-unit` in the template.
;;
;; This snippet uses that element as the title template, replacing the
;; `title` attribute of it, and its textual content with the title of
;; the page itself.
(h/defsnippet blog-page-title (cfg/template) [:.hero-unit :h1]
  [title]
  [:h1] (h/do->
         (h/content title)
         (h/set-attr :title title)))

;; If commenting is enabled for a post, the `#disqus` element should
;; be left intact, as-is. Otherwise, it will be removed, that is all
;; this snippet does.
(h/defsnippet blog-page-disqus (cfg/template) [:#disqus]
  [page]

  [:#disqus] (when (:comments page) identity))

;; #### Putting it all together
;;
;; To put a full page together, we alter the page title, disable the
;; recent and archived post areas, along with the `#post-neighbours`,
;; as pages do not have those. We also rearrange the `hero-unit`, and
;; last but not least, fill out the sidebar, using the tools provided
;; by [blog.nav][1].
;;
;; [1]: #madness.blog.nav
;;
(h/deftemplate blog-page (cfg/template)
  [page all-posts]

  [:title] (h/content (:title page) " - Asylum")
  [:#recents] nil
  [:#archive] nil
  [:#post-neighbours] nil
  [:.hero-unit] (h/do->
                 (h/content (blog-page-title (:title page))
                            (:content page)
                            (blog-page-disqus page)))
  [:#nav-recent-posts :ul :li] (blog-nav/recent-posts all-posts)
  [:#nav-tags :ul :li] (blog-nav/all-tags all-posts))
