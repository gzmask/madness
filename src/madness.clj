;; ## Madness
;;
;; Madness is a static site generator tool, primarily aimed at
;; generating a blog, but also supports static pages too. It was
;; primarily built to support my own needs when I rebuilt
;; [my own site][1], but perhaps it may prove useful for others
;; aswell.
;;
;; Source is available on [github][2], and this documentation is also
;; available [online][3].
;;
;; The source code, the design and the posts on the `asylum` branch
;; are all under a [CC-BY-SA-3.0][5] license.
;;
;; [1]: http://asylum.madhouse-project.org/
;; [2]: https://github.com/algernon/madness
;; [3]: http://algernon.github.io/madness/
;; [5]: http://creativecommons.org/licenses/by-sa/3.0/
;;
;; ### Requirements
;;
;; I only had a few simple requirements when I looked for a static
;; site generator:
;;
;; * **Simple** updating: I do not want a complicated process, one
;;   that involves copying one directory over another. If I want to
;;   upgrade from one version of the generator tool to the other, all
;;   I want to do, is merge, and resolve conflicts. Perhaps update my
;;   templates, but that's about it.
;;
;;   I have a version control system for a reason, layering over that
;;   is counter productive.
;;
;; * For similar reasons, I absolutely hate when templates are split
;;   into many many small files. I prefer them in one, as long as that
;;   makes sense.
;;
;;   Since I do not need neither plugins, nor anything fancy like
;;   that, this works very well.
;;
;; * The templates must not contain any code, at all. They're plain
;;   HTML mocks, at most with ids or classes added so that the
;;   generator can easily recognise parts of the template. But no code
;;   ever, should be allowed in a template. This rules out pretty much
;;   every templating language out there.
;;
;;   Thankfully, with Enlive, this goal could be easily achieved.
;;
;; * The engine must also support per-tag feeds, because if and when I
;;   decide to start publishing to a planet, it makes sense to only
;;   publish those parts of my blog, that are relevant for that
;;   particular planet.
;;
;; ### Features
;;
;; I found no static site generator that supported all of the above,
;; and came with a reasonable theme aswell, so I ended up writing my
;; own engine, and my own theme, and ended up with the following
;; features:
;;
;; * Easy to update: I work on two branches, `master` and
;;   `asylum`. The former has no templates, no posts, no pages, just
;;   the code. The latter is all about the templates, posts, pages,
;;   and has no extra code.
;;
;;   Therefore, merging between them is easy. If there'd be any
;;   outside contributions, either to the theme, or the code, those
;;   would be straightforward to handle too.
;;
;; * Single template for a single output format. I have no plugins to
;;   support, no need to include bits and pieces from other files. I
;;   can afford to have a single template, that I can preview in a
;;   browser as-is, and immediately do changes there.
;;
;; * The templates are pure data, there is no code in them. Every bit
;;   of logic is within the code itself, the templates are pure.
;;
;; * The engine supports per-tag archives and per-tag feeds,
;;   implementing date-based archives or feeds wouldn't be hard,
;;   either.
;;
;; * It is written in a sane language, one that I feel comfortable
;;   with, one I can understand months later, even if I didn't write
;;   neither tests nor documentation originally (I really should have
;;   done both, though, out of principle if nothing else).
;;
;; ### The templates
;;
;; While the main `master` branch does not have any default template,
;; the `asylum` branch does: one built upon Twitter's Bootstrap, in
;; such a way that it displays well on any display ranging from
;; smartphones to huge desktops.
;;
;; At least, that was the plan, and that's how it works on everything
;; I tested it with so far.
;;
;; It's responsive, straightforward, and quite simple too, as far as I
;; can tell. It also degrades well even down to text-mode browsers.
;;

(ns madness)
