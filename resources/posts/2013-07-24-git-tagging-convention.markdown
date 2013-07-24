---
title: "My git tagging convention"
date: 2013-07-25 17:00
tags: [Technology, Rants, .planet.debian]
---

[Zigo][blog:zigo] posted an [article][blog:v-disease] earlier, one
which I disagree with strongly. First of all, something that has been
in use ever since git was invented is hardly a new fashion. Something
that is used by the very same person who invented git and by git
itself, is hardly surprising to be found so widespread. Especially not
when it is used as an example in git's very own documentation! Writing
it off as something silly, because of the way GitHub works, without
even trying to understand the reason behind it is just plain wrong.
Just because GitHub names tarballs the way it does, does not mean that
we should change the way we tag.

For example, [cgit][cgit] (used by, among others,
[kernel.org](http://git.kernel.org/cgit)), uses the tag name as-is for
its tarballs, unlike GitHub which prepends the project name, so one
will have to mangle the filename in a <code>debian/watch</code> file
anyway, when downloading from a host that is not GitHub, therefore his
arguments for a raw version-only tag are bogus, unless your upstream
is using GitHub.

But I do not want to criticise only, rather, to provide a reason *why*
a prefix is used, and why I chose the "worst" prefix: the project name
itself.

 [blog:zigo]: http://thomas.goirand.fr/blog/
 [blog:v-disease]: http://thomas.goirand.fr/blog/?p=121
 [cgit]: https://github.com/kevclark/cgit

<!-- more -->

One very strong reason to use a prefix, either **v** or any other
prefix is to ease tab completion. If you have a tag that is a bare
number, you type the first, press tab, and get a mixture of commits
and tags. You can't easily tell your completion system that you want
commits or tags. With a prefix outside of the hex range, which **v**
is, you can do that, and that makes working with it a lot easier.

Is it for convenience? Yes. But ask yourself this: how many times do
you have to write a <code>debian/watch</code> file? Once per package.
How much time do you spend looking for a tag, or a commit? A lot more.
So which one is most important? The convenience of someone who has to
work around the tarball naming of the hosting service once, or the
developer who works with the software daily? Obviously the latter!

But that is not all. I'm an advocate of prefixing the tag with the
project name, actually, and I've been doing that for all new projects
for a while now, and I'm not going to change that, because it serves a
very practical purpose: if you have a parent repository, with a lot of
submodules, if git tag tells you the project name too, that makes it
much easier to navigate. I don't have to bake submodule support into
my shell prompt (that would be costy), and I won't find it surprising
that if I enter a subdirectory, <code>git tag</code> fails to list the
version I know I'm working on. Just because I happened to end up in a
submodule, which is something I end up doing often, as I have many
repositories that have submodules, which in turn have other
submodules, and so on and so forth through many layers.

A raw version number as a tag name is insufficient for my needs,
simple as that. And I, as upstream, don't care that whoever packages
my thing, will have to add a line to a watch file. That only needs to
be done once, while I work with tags and commits daily.

I'm sorry, but calling this naming convention a disease just because
GitHub's tarball naming is what it is, without even considering that
there may be other reasons behind it than legacy, and that there are
hosting sites outside of GitHub, is a mistake. Dear package
maintainers, please do not annoy us upstreams with requests to cripple
our daily work. Thank you.
