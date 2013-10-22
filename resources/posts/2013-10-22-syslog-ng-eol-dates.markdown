---
title: "syslog-ng OSE supported versions and EoL times"
date: 2013-10-22 11:30
tags: [News, syslog-ng]
---

We've been thinking about how to approach the problem of supporting
syslog-ng OSE versions, the end-of-life of them, and related issues.
I've been maintaining syslog-ng branches for about a year and a half
now, and the time and effort required to do that is only becoming
clear now that I have a reasonable sample size.

There was no clear communication about how long each version of
syslog-ng OSE would be supported, and when they will reach their end
of life, which made it harder for distributions and pretty much
everyone else to plan ahead and schedule time for migrating to a new
version. We intend to rectify that now, and my plan is outlined below:

We would - at any one time - maintain four branches of syslog-ng
(including the bleeding edge development branch). The other three would
be: a critical-bug-fix-only maintenance release (this is currently 3.3),
a recommended stable release (3.4), and the upcoming feature release
(3.5beta at the moment). Whenever the development branch gets a beta
release, it would automatically become the upcoming feature release, the
former feature release would get promoted to stable, and the former
stable becomes the maintenance branch.

<!-- more -->

In this scheme, 3.3 would be maintained until 3.6.0beta1, at which
point, it would be dropped from the supported set, and 3.4 would take
its place, and 3.5 would become the recommended stable release.

Of course, it's still hard to plan ahead without dates, but dates are
something I cannot give at this point. The best I can do is send an
announcement once 3.6 hits the alpha stage, and give a rough estimate on
when it would hit beta. That's a few months in advance, and by that
time, 3.4 will be out long enough for folk to migrate over, thus 3.3
shouldn't be in much use anymore.

With the current pace of development, this means that a syslog-ng OSE
release will be maintained for about 2-2.5 years, and I expect it will
not go down by much more than half a year, meaning 1.5-2 years of
maintenance.

And to make it clear what each branch means, let me expand on that a
bit:

* **Maintenance**: The former stable branch, which receives critical
  bug fixes only. People are strongly encouraged to migrate from this
  to the stable one, but the branch is still supported to some extent,
  to make it easier for distributions, among others. Critical bugs are
  security issues, serious regressions, and so on - I reserve the
  right to decide which bugs to consider critical. Fixes from any
  other branch are not backported, unless they're critical. Currently
  the 3.3 branch fills this role.

* **Stable**: The stable branch, receiving all kinds of bug fixes,
  including smaller fixes backported from newer branches (including
  the next feature release and the development branch too). It may
  also receive very small features too, which do not affect the
  package as a whole: things like the $(format-json) improvement to
  allow comma-separated scopes, or being able to specify a template
  for the JSON parser. Currently the 3.4 branch fills this role.

* **Feature**: The next stable branch, which we consider stable
  enough, but has not received as wide testing as the stable branch
  yet. This gets the most attention, as the idea is to stabilise it as
  soon as possible. Other than being newer than the Stable branch,
  this is treated in exactly the same way. Currently the 3.5 branch
  fills this role.

* **Development**: The branch where all development happens. While we
  try to keep it stable, regressions and breakage is more likely here,
  and new features are not guaranteed to remain the same during the
  development cycle, so building production on this branch is not
  advised. New features are developed against this branch, and the
  [Incubator's][1] master branch follows the development branch too.
  The 3.6 branch will fill this role, once opened.

As for the [Incubator][1]: it will only have two branches: one for the
latest feature release, and another for development. Maintenance and
Stable versions of syslog-ng will not be supported in there.

 [1]: https://github.com/algernon/syslog-ng-incubator

The Incubator is a place for experimental modules that are either not
ready to be merged into syslog-ng proper, or are kept separate for any
other reason.
