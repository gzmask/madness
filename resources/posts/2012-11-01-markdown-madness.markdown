---
title: Markdown madness
date: 2012-11-01 15:00
tags: [News, Technology, Clojure]
comments: 1
---

When I [migrated][1] this site to my own [engine][2], I lost support
for Markdown sources, and converted everyting I have to plain old HTML
snippets. This worked for a while, but whenever I was writing a post
with long links, and only very little formatting, I missed
markdown. 

 [1]: /blog/2012/06/27/madness/
 [2]: http://algernon.github.com/madness/

<!-- more -->

So I sat down, and added a few dozen lines of code, that when it sees
a markdown file, will run turn it into HTML, and rearrange it in such
a way that the existing HTML processing engine can work with it just
fine. This post, for example, is written in Markdown, and the code is
set up so that adding other kinds of soruces is fairly easy, as long
as they can be converted to HTML. Adding, say, ReStructuredText (or
even LaTeX!) should not be too hard, and I may end up doing just that,
since I have a large set of documents in LaTeX that I'd love to
publish on this site too, in HTML format.

But back to markdown!

I tried two different libraries that can turn Markdown into HTML:
[markdown-clj][3] and [pegdown][4], and while I would've loved to use
the former, it does not support link references (yet?). When writing
links, I much prefer separating the target and the link text: it makes
the text flow easier to read when one's less interested in the link
targets themselves. So I'm an avid user of the `[text][ref]`-style,
and only the latter, [pegdown][4] supported that.

 [3]: https://github.com/yogthos/markdown-clj
 [4]: https://github.com/sirthias/pegdown
