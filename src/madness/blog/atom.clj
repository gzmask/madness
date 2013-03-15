(ns madness.blog.atom
  "## Rendering Atom feeds

  Atom feeds are considerably easier to render than HTML pages, there
  is much less markup and design to pay attention to."

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "Creative Commons Attribution-ShareAlike 3.0"
              :url "http://creativecommons.org/licenses/by-sa/3.0/"}}

  (:require [clj-time.format :as time-format]
            [clj-time.local :as time-local]
            [net.cgrand.enlive-html :as h]
            [clojure.string :as str]
            [madness.config :as cfg]))

;; Atom feeds need a special date format, this is that one.
(def atom-date-formatter
  (time-format/formatter "yyyy-MM-dd'T'HH:mm:ssZZ"))

;; We'll need to include the full post in the Atom feed, the summary
;; and the content after one another, but we need to render only
;; those, and those alone, without the rest of the design.
;;
;; For this, we use an empty template, and replace it wholly with the
;; rendered summary + content combination.
(h/deftemplate bare-post (cfg/template :empty)
  [post]

  [:html] (h/substitute (:summary post) (:content post)))

(defn local-href-expand
  "Since Atom feeds are published, links therein should be
  absolute. This little function replaces all local links - any, that
  starts with / - with their expanded, fully qualified version."

  [feed]

  (str/replace feed #"href=\"(/[^\"]*)\""
               (str "href=\"" (cfg/atom-feed :base-url) "$1\"")))

;; Atom entries have categories, this snippet takes the category
;; element of an entry from the template, and fills it out, according
;; to the tag given as a parameter.
(h/defsnippet atom-post-tag (cfg/template :atom) [:entry :category]
  [tag]

  [:category] (h/do->
               (h/set-attr :term (str/replace (str/lower-case tag) " " "-"))
               (h/set-attr :label tag)))

;; A single atom post has a `title`, a `link`, an `updated` and
;; `published` date, an `id`, `content` and multiple `category`
;; elements. This snippet takes the `entry` element from the source
;; template, and fills it in appropriately.
(h/defsnippet atom-post (cfg/template :atom) [:entry]
  [site-base post]

  [:title] (h/content (:title post))
  [:link] (h/set-attr :href (str site-base (:url post)))
  [:updated] (h/content (time-format/unparse
                         atom-date-formatter (time-local/to-local-date-time (:date post))))
  [:published] (h/content (time-format/unparse
                           atom-date-formatter (time-local/to-local-date-time (:date post))))
  [:id] (h/content (str site-base (:url post)))
  [:category] (h/clone-for
               [tag (:tags post)]
               (h/substitute (atom-post-tag tag)))
  [:content] (h/content (apply str (bare-post post))))

;; Finally, assembling the whole atom feed is as simple as setting a
;; the `title` and `id` under `feed`, with the site title and our base
;; uri, respectively, updating the `updated` element, and adding the
;; entries, by cloning the `atom-post` snippet above for each entry
;; within the feed.
;;
;; The Atom feed also has two links: one with `rel=self`, and another,
;; the base URI. The former must be marked with a `#self` id in the
;; source template, the latter with `#base`. The extra ids will be
;; removed from the result, they're only there so that the parts can
;; be easily identified within the template.
;;
(h/deftemplate atom-feed (cfg/template :atom)
  [title uri site-base posts]

  [:feed :title] (h/content title)
  [:feed] (h/set-attr :xmlns "http://www.w3.org/2005/Atom")
  [:#self] (h/do->
            (h/remove-attr :id)
            (h/set-attr :href (str site-base uri "atom.xml")))
  [:#base] (h/do->
            (h/remove-attr :id)
            (h/set-attr :href (str site-base uri)))
  [:feed :id]
    (h/content (str site-base uri))
  [:updated] (h/content (time-format/unparse atom-date-formatter (time-local/local-now)))
  [:entry] (h/clone-for [p posts]
                        (h/substitute (atom-post site-base p))))

(defn emit-atom
  "To finalise the Atom feed, local links shall be expanded, after the
  feed has been generated. This function puts that all together, and
  should be the only function used from this namespace."
  
  [title uri posts]

  (local-href-expand (apply str (atom-feed title uri
                                           (cfg/atom-feed :base-url) posts))))
