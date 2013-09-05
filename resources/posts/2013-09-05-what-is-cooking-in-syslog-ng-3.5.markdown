---
title: "What's cooking in syslog-ng 3.5?"
date: 2013-09-05 16:00
tags: [Technology, syslog-ng, .planet.debian]
---

The first alpha of [syslog-ng 3.4][sng:3.4] was released more than a
[year ago][sng:3.4:alpha1], and the first stable release
[earlier this year][sng:3.4.1], development on
[syslog-ng 3.5][sng:3.5] has been going on steadlily since then, with
a lot of interesting features and changes pouring in. While the 3.5
release will not bring as many overwhelming changes as the previous
two releases did, the sheer number of improvements is still
substantial. Our focus was on smaller developments all over the place
this time, instead of big sweeping changes. In this post, we will go
over most of these features, with use-cases, examples and a few words
about why they are so useful and awesome.

[Multi-line support][f:multiline], [new destinations][f:new-dests],
[new template functions][f:template-funcs],
[type hinting][f:type-hinting], [unit suffixes][f:unit-suffix], and
[other things][f:misc]. Read on, and be amazed!

 [sng:3.4]: https://github.com/balabit/syslog-ng-3.4
 [sng:3.4:alpha1]: http://permalink.gmane.org/gmane.comp.sysutils.syslog-ng.announce/136
 [sng:3.4.1]: https://lists.balabit.hu/pipermail/syslog-ng-announce/2013-January/000154.html
 [sng:3.5]: https://github.com/balabit/syslog-ng-3.5

 [f:multiline]: /blog/2013/09/05/what-is-cooking-in-syslog-ng-3.5/#f:multiline
 [f:new-dests]: /blog/2013/09/05/what-is-cooking-in-syslog-ng-3.5/#f:new-dests
 [f:template-funcs]: /blog/2013/09/05/what-is-cooking-in-syslog-ng-3.5/#f:template-funcs
 [f:type-hinting]: /blog/2013/09/05/what-is-cooking-in-syslog-ng-3.5/#f:type-hinting
 [f:unit-suffix]: /blog/2013/09/05/what-is-cooking-in-syslog-ng-3.5/#f:unit-suffix
 [f:misc]: /blog/2013/09/05/what-is-cooking-in-syslog-ng-3.5/#f:misc

<!-- more -->

These are only bigger features, there has been a lot of small tweaking
going on, and of course, all of the fixes that went into prior
versions will also be in 3.5 too, and there's still a chance that
something else may find its way in before the feature freeze.

### <a name="f:multiline" href="#f:multiline">Multi-line support</a>

One of the major new features in the 3.5 release will be support for
multi-line messages, a feature that has been available in
[syslog-ng PE][sng:PE] for a good while, and which has been ported to
and improved upon to the open source edition. Two variants of
multi-line are supported, which will be detailed below. Both of them
are available for the <code>file()</code> and <code>pipe()</code>
sources only.

 [sng:PE]: https://www.balabit.com/network-security/syslog-ng/central-syslog-server

#### Indented multi-line

The easiest variant is indented multi-line, where each line can be
followed by others, indented by whitespace, and the message continues
until the first non-indented line. This is the format used by the
Linux kernel too, from version 3.5, for <code>/dev/log</code>. This
type of multi-line can be used as follows:

<pre>source s_multiline { file("/path/to/file" multi-line-mode(indented)); };</pre>

Consider that <code>/path/to/file</code> has the following content:

<pre>First line
 Continuation 1;
 Continuation 2;
Second line</pre>

With the <em>indented</em> <code>multi-line-mode()</code> setting,
this would turn into two log messages:

<pre>First line\n Continuation 1;\n Continuation 2;
Second line</pre>

#### Regexp-based multiline

If multi-line input is not based on indentation, one can use the
<em>regexp</em> <code>multi-line-mode()</code> instead, which makes
two new settings available: <code>multi-line-prefix()</code> and
<code>multi-line-garbage()</code>. These can be used to define the
start and the end of a log message: any string between a the beginning
matching prefix and a matching garbage will be considered a single
message. That is, the prefix will be included, the garbage will not
be: it will be discarded.

To illustrate:

<pre>source s_multiline {
 file("/path/to/file" multi-line-mode(regexp)
                      multi-line-prefix("^prefix")
                      multi-line-garbage(" garbage$"));
};</pre>

If the source contains these lines:

<pre>prefix message
continuing garbage</pre>

This will turn into the following log message:

<pre>prefix message\ncontinuing</pre>

### <a name="f:new-dests" href="#f:new-dests">New destinations</a>

It would be pretty hard to do a syslog-ng release without new
destinations, and with 3.5, we will have three of them!

#### STOMP

While we had AMQP before, we now have a [STOMP][stomp] destination
too, so we can stream logs to any STOMP server, with a few simple lines:

 [stomp]: http://stomp.github.io/

<pre>stomp(
 host("localhost")
 port(61613)
 destination("/topic/syslog")
 routing_key("")
 persistent(yes)
 ack(no)
 username("someone")
 password("something")
 body("This is the body! YAY! Here's a message: $MESSAGE!\n")
 value-pairs(scope(nv-pairs, syslog-proto, selected-macros))
)</pre>

Of course, none of these settings need to be set, you can just use the
defaults, and it will just work!

#### Riemann

Did you know that syslog-ng is far more than a log collector and
processor? No? You do now. When you have access to a lot of logs, and
a tremendous amount of power in parsing them, you can use these tools
for monitoring easily! And when we're talking monitoring,
[riemann][riemann] is a great asset in our toolbox, and with the new
destination, we can easily forward metrics to it:

 [riemann]: http://riemann.io/

<pre>riemann(
 server("localhost")
 port(5555)

 host("$HOST")
 service("$PROGRAM")
 description("$PROGRAM pids")
 metric(int("$PID"))
 ttl(300)
);</pre>

Of course, the above is a very silly example, it would make much more
sense to extract data from a log message instead, but this will serve
as an example.

### <a name="f:template-funcs" href="#f:template-funcs">New template functions</a>

<dl>
 <dt><code>$(upper-case <em>TEXT...</em>)</code>, <code>$(lower-case <em>TEXT...</em>)</code></dt>
 <dd>
  To turn some text into *upper-* or *lower-case*, these two template
  functions come in handy. They simply turn all their arguments into
  the respecitve case.
 </dd>

 <dt><code>$(delimit <em>DELIMITERS</em> <em>NEW-DELIMITER</em> <em>TEXT</em>)</code></dt>
 <dd>
  Sometimes one has a delimited string, where one wishes to replace
  the delimiters. This function does just that: give it a list of
  delimiters (you may need to quote it, if it contains whitespace), a
  replacement, and a text to replace delimiters in, and it does the
  rest.

  For example, to replace tabs and spaces with a vertical bar, one
  could write a template like this:

  <pre>template("$(delimit \"\t \" \"|\" $MESSAGE)\n")</pre>
 </dd>

 <dt><code>$(env <em>VARIABLE...</em>)</code></dt>
 <dd>
  Have you ever felt the need to check an environment variable from
  within syslog-ng? I did, and now I can.
 </dd>
</dl>

### <a name="f:type-hinting" href="#f:type-hinting">Type hinting</a>

While syslog-ng supported sending log messages into various data
stores and message queues for a while (SQL at first, MongoDB, JSON and
AMQP later), even when those supported different types of data than
strings, we could not do anything else. Until now. It is now possible
- at places where it makes sense - to annotate templates with type
hints, which the destination driver can optionally use. Type hinting
is implemented for the <code>mongodb()</code> destination and the
<code>$(format-json)</code> template function for now. When no type
hint is specified, syslog-ng defaults to *string*.

To add type hints, simply wrap the respective template with the hinted
type, like this:

<pre>mongodb(
 value-pairs(pair("date", datetime("$UNIXDATE"))
             pair("pid", int64("$PID")))
);</pre>

Currently the following type hints exist: *boolean* (anything that
begins with a *t* or *1* is true, anything that begins with *f* or *0*
is false, everything else is an error), *string*, *literal* (same as
string, but not quoted if it would be quoted otherwise), *int32*
(*int* is an alias for this), *int64*, and *datetime*. Only UNIX
timestamps can be type-hinted to *datetime*, anything else will likely
result in a casting error.

It is also possible to control what happens when type casting fails:
syslog-ng can drop the whole message, drop the property, or fall back
to string. It can also do all of these silently:

<pre>options {
 typecast(on-error(silently-drop-property));
};</pre>

Using this feature with <code>$(format-json)</code> is very similary
too:

<pre>$(format-json date=datetime("$UNIXDATE") pid=int64("$PID"))</pre>

With this feature in place, you can now store your non-string values
with their proper types!

### <a name="f:unit-suffix" href="#f:unit-suffix">Unit suffixes</a>

Unit suffixes make it considerably easier to set limits and describe
numbers within the syslog-ng configuration. We no longer need to spell
out sizes to the byte precious, it is now enough to write:
<code>log-fifo-size(200MiB)</code>. Now, syslog-ng will understand
suffixes for kilo-, mega-, and giga-bytes, (*K*, *M*, *G*,
respectively) either in base-10 or base-2 (with an extra *i* after the
suffix). One can also omit the trailing *b* from the end.

So, to set the <code>log-fifo-size()</code> to *2097152* bytes, one
can simply use *2MiB*. Or, to set it to *2000000*, *2Mb*. That's a
whole lot easier, isn't it? No more counting zeros, no more silly
typos in a ten-digit number, no more pain, but easily readable units!

### <a name="f:misc" href="#f:misc">Miscellaneous features</a>

Apart from the features above, there have been a lot of other changes
and improvements in the code base:

<ul>
 <li>
  We now have a (mostly) non-recursive, quiet build system, which is
  not only much faster than what we had before, but more reliable too,
  and easier to glance through its logs, too.
 </li>

 <li>
  A few old settings that were never used were removed (the
  <code>username()</code> and <code>password()</code> settings of the
  <code>mongodb()</code> driver), some were renamed and deprecated:
  the <code>replace()</code> key transformation function of
  <code>value-pairs()</code> was renamed to
  <code>replace-prefix()</code>, as that makes the intent clearer. In
  this latter case, the old name is still valid, but obsoleted.
 </li>

 <li>
  Our integration with <a href="http://freedesktop.org/wiki/Software/systemd/">systemd</a> is
  much tighter now: syslog-ng can notify systemd when it is ready.
  This also means that when systemd support is enabled, certain
  systemd libraries will have to be installed: syslog-ng no longer
  carries a convenience copy.
 </li>

 <li>
  A new filter was implemented: <code>in-list</code>, with which one
  can implement efficient white- or blacklists.

  To use it, you will need a file with one value a line, and do
  something along these lines:

  <pre>filter f_whitelist {
  in-list("/path/to/file.list", value("PROGRAM"));
};</pre>

  To do a blacklist, just negate the filter:

  <pre>filter f_blacklist {
  not in-list("/path/to/file.list", value("HOST"));
};</pre>

 </li>

 <li>
  syslog-ng now supports Linux 3.5+-style <code>/dev/kmsg</code>, and
  will use that instead of <code>/proc/kmsg</code> when a sufficiently
  recent kernel is detected (assuming one is using the
  <code>system()</code> source).

  The new kernel log format supports structured messages, and
  syslog-ng is smart enough to parse them, and make them accessible
  like all other message properties (with a <em>.linux.</em> prefix).
 </li>
</ul>
