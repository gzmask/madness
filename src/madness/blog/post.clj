(ns madness.blog.post
  "## Loading & rendering individual blog posts

  For the sake of ease, blog posts are fully loaded first, and turned
  into a structure that is easy to work with. This namespace
  implements the low-level loading, restructuring and rendering of
  individual blog posts.

  Blog posts are under `resources/posts` by default, and they all must
  have a filename that starts with a date (in YYYY-MM-DD format),
  followed by the short title of the post, that will be used for the
  URL."

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "Creative Commons Attribution-ShareAlike 3.0"
              :url "http://creativecommons.org/licenses/by-sa/3.0/"}}
  
  (:require [net.cgrand.enlive-html :as h]
            [madness.blog.nav :as blog-nav]
            [madness.utils :as utils]
            [madness.config :as cfg]
            [clojure.string :as str]
            [madness.io :as io]
            [clj-time.format :as time-format]))

;; The date format used within blog files. Note that this is not the
;; format we render dates as, but the format we expect them to be
;; within blog posts!
(def blog-date-format ^{:private true}
  (time-format/formatter "yyyy-MM-dd HH:mm"))

(defn- post-url
  "Given a parsed date and a source filename, return the URI for the blog post.

  The source filenames will be stripped of the date part, and the date
  within the post itself will be used to construct the final URL of
  the form `/blog/yyyy/MM/dd/title/`."

  [date fn]

  (str "/blog/" (time-format/unparse (time-format/formatter "yyyy/MM/dd") date)
       "/" (second (first (re-seq #"....-..-..-(.*)\.([^\.]*)$" fn))) "/"))

(defn read-post
  "Read a blog post from a file, and restructure it into a
  representation that is easy to work with.

  Each blog post must have an `article` element, which must also have
  `title`, `date`, and `tags` children - all of their purpose should
  already be clear. The `article` element may also have a `comments`
  propery, which, when set, will enable commenting on the particular
  post.

  A blog post must also have a `summary` element, the contents of
  which will be used when rendering the post for the purposes of
  recent posts, or as the summary on the main index page. The summary
  is also part of the blog post, and when viewing the entire post, it
  will start with the summary.

  Following that, the entire contents of the `section` element of the
  blog post will be displayed.

  The structure this function generates, should be pretty clear by
  glancing over the code here."

  [file]

  (let [post (io/read-file file)
        date (time-format/parse blog-date-format (apply h/text (h/select post [:article :date])))]
    {:title (apply h/text (h/select post [:article :title])),
     :tags (map h/text (h/select post [:article :tags :tag])),
     :summary (h/select post [:summary :> h/any-node]),
     :date date,
     :url (post-url date (.getName file))
     :comments (or
                (-> (first (h/select post [:article])) :attrs :comments utils/enabled?)
                (-> (h/text (first (h/select post [:article :comments]))) utils/enabled?)),
     :content (h/select post [:section])}))

;; ### Blog post templates
;;
;; Blog posts are almost entirely contained within a `hero-unit`
;; class, with only the previous/next links outside of it.

;; One of the first things one sees about a post, is its title, which
;; is the `h1` element of the `hero-unit` in the template.
;;
;; This snippet uses that element as the title template, replacing the
;; `title` attribute of it, and its textual content with the title of
;; the post itself.
(h/defsnippet blog-post-title (cfg/template) [:.hero-unit :h1]
  [title]
  [:h1] (h/do->
         (h/content title)
         (h/set-attr :title title)))

;; #### Full article footer
;;
;; Posts, when viewed in full, and not only their summary, have a
;; footer, which holds their tags and the date they were posted on,
;; these all live under the `#full-article-footer` element.

;; The full article footer has only a single link in the template: the
;; snippet that we'll use to render a single tag.
(h/defsnippet blog-post-tag (cfg/template) [:#full-article-footer :a]
  [tag]

  [:a] (h/do->
        (h/set-attr :href (utils/tag-to-url tag))
        (h/after " "))
  [:a :span] (h/substitute tag))

;; The full footer contains a date, the `#post-date` element, and
;; we'll also render the tags there too. Both the `#post-date` and the
;; `#full-article-footer` ids will be removed.
(h/defsnippet blog-post-footer (cfg/template) [:#full-article-footer]
  [post]

  [:a] (h/clone-for [tag (:tags post)]
                    (h/substitute (blog-post-tag tag)))
  [:#post-date] (h/do->
                 (h/content (utils/date-format (:date post)))
                 (h/remove-attr :id))
  [:#full-article-footer] (h/remove-attr :id))

;; #### Post navigation
;;
;; To ease navigating between posts, previous and next posts (if
;; available) will be shown outside of the `hero-unit`. These we'll
;; call `#post-neighbours`, and this element must have two children:
;; one with a `pull-left` class (for the next post), and another with
;; `pull-right` (for the previous post).
;;
;; Both of these need to have an `a` element, whose `href` will be
;; rewritten, and that element must have a `span` child, to be
;; replaced by the title of the previous or next post.
;;
;; The `#post-neighbours` id will be removed from the final rendering,
;; as it is only used to easily identify the snippet within the full
;; template.
(h/defsnippet blog-post-neighbours (cfg/template) [:#post-neighbours]
  [neighbours]

  [:.pull-left :a :span] (h/substitute (:title (first neighbours)))
  [:.pull-left :a] (h/set-attr :href (:url (first neighbours)))
  [:.pull-left] (if (empty? (first neighbours))
                  nil
                  identity)
  
  [:.pull-right :a :span] (h/substitute (:title (last neighbours)))
  [:.pull-right :a] (h/set-attr :href (:url (last neighbours)))
  [:.pull-right] (if (empty? (last neighbours))
                   nil
                   identity)
  [:#post-neighbours] (h/remove-attr :id))

;; #### Commenting
;;
;; If commenting is enabled for a post, the `#disqus` element should
;; be left intact, as-is. Otherwise, it will be removed, that is all
;; this snippet does.
(h/defsnippet blog-post-disqus (cfg/template) [:#disqus]
  [post]

  [:#disqus] (when (:comments post) identity))

;; #### Putting it all together
;;
;; To put a full blog post together, we alter the page title, disable
;; the recent and archived post areas, rearrange the `hero-unit`, pull
;; in the next/prev links into `#post-neighbours`, and last but not
;; least, fill out the sidebar, using the tools provided by
;; [blog.nav][1].
;;
;; [1]: #madness.blog.nav
;;
(h/deftemplate blog-post (cfg/template)
  [post all-posts]

  [:title] (h/content (:title post) " - Asylum")
  [:#recents] nil
  [:#archive] nil
  [:.hero-unit] (h/do->
                 (h/content (blog-post-title (:title post))
                            (:summary post)
                            (:content post)
                            (blog-post-footer post)
                            (blog-post-disqus post)))
  [:#post-neighbours] (h/substitute (blog-post-neighbours (utils/neighbours all-posts post)))
  [:#nav-recent-posts :ul :li] (blog-nav/recent-posts all-posts)
  [:#nav-tags :ul :li] (blog-nav/all-tags all-posts))
