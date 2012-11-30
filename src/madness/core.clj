(ns madness.core
  "Entry point into madness, usage:

    $ lein run -m madness.core
    $ lein run -m madness.core :index :archive"

  ^{:author "Gergely Nagy <algernon@madhouse-project.org>"
    :copyright "Copyright (C) 2012 Gergely Nagy <algernon@madhouse-project.org>"
    :license {:name "Creative Commons Attribution-ShareAlike 3.0"
              :url "http://creativecommons.org/licenses/by-sa/3.0/"}}
  
  (:require [madness.render :as render]))

(defn- str->keyword
  "Takes a string, strips the first char (assumed to be `:`), and
  returns a keyword."

  [s]

  (keyword (apply str (rest s))))

(defn -main
  "The main entry point of Madness: when called without arguments,
  generates everything. If called with a list of (string) keywords,
  only generates the given parts of the blog.

  The following keywords are understood:

  * `:index`: The main index page of the blog.
  * `:archive`: The main archive page of the blog.
  * `:tags`: All of the per-tag archives of the blog.
  * `:date-archives`: Yearly, monthly & daily archives of the blog.
  * `:posts`: All of the posts that belong to the blog.
  * `:pages`: All of the pages that accompany the blog.
  * `:main-feed`: The main Atom feed.
  * `:tag-feeds`: The per-tag Atom feeds.
  * `:date-feeds`: Yearly, monthly & daily atom feeds."
  
  ([] (-main ":index" ":archive" ":tags" ":date-archives" ":posts"
             ":main-feed" ":pages" ":tag-feeds" ":date-feeds"))
  ([& args] (dorun (map render/render (map str->keyword args)))))
