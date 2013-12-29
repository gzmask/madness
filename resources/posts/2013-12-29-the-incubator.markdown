---
title: "Introducing the syslog-ng Incubator"
date: 2013-12-29 04:20
tags: [syslog-ng, HOWTO, .planet.debian]
---

When [syslog-ng 3.5.1][sng:351] was released, the existence of the
[syslog-ng Incubator][sng:incubator] project was already announced,
but I did not go into detail as to why it exists, what is in there,
how to build it, or how to use it. The documentation that exists on it
is almost non-existent, and what does exist, is usually in the form of
a commit message and some example files. But there's nothing on how
the things in the Incubator can be used, and what problems they solve.

With this post, I will try to ease the situation a little, and provide
some insight on how I use some of the things in there, like the
[Riemann destination][sng:riemann].

 [sng:351]: /blog/2013/11/06/syslog-ng-3.5.1/
 [sng:incubator]: https://github.com/balabit/syslog-ng-incubator
 [sng:riemann]: https://github.com/balabit/syslog-ng-incubator/tree/master/modules/riemann/

<!-- more -->

## About the Incubator

The idea for the [Incubator][sng:incubator] existed for a long time,
the first incarnation of it was called *syslog-ng module collection*,
and had a different scope: it was a playing field not only for new
modules, but updates to existing ones too. The idea was to try out
new, possibly experimental features separately from syslog-ng. That
didn't work well: it was a nightmare to maintain, and even harder to
use. So it never got anywhere, as the developments made there were
quickly merged into syslog-ng itself anyway.

Then, two years later, I had a need for a new destination, but there
was no open development branch to base it against, and forking and
maintaining a whole syslog-ng branch for the purpose of a single
module sounded like an overkill. So the Incubator was born, from the
ashes of the failed module collection.

The purpose of the Incubator is to be a place of experimental modules,
and an easier way to enter the world syslog-ng development. It's
something with far less requirements than syslog-ng (I do not care all
that much about portability when it comes to the Incubator, especially
not to horrible abominations like HP-UX or AIX), less rules, and more
freedom. It's meant to be more developer friendly than syslog-ng
proper.

It also serves as an example of developing modules completely external
to syslog-ng, and parts of it will be used in future posts of mine, to
demonstrate the development of new modules.

## Compiling & Installation

The first thing we need to do, is to compile whatever we need from the
[Incubator][sng:incubator], unless we happen to have pre-built
binaries available (which are going to appear in your usual
third-party syslog-ng repositories over the next few weeks). For this,
at a minimum, one will need [syslog-ng 3.5+][sng] and [ivykis][ivykis]
installed, along with autotools (autoconf, automake and libtool),
pkg-config, bison, and depending on which modules one needs,
[riemann-c-client][rcc] and [libmongo-client][lmc] too. Most of these
are available packaged in better distributions, but only in recent
versions, so one may need to compile those too. They follow a very
similar scheme, though.

 [sng:incubator]: https://github.com/balabit/syslog-ng-incubator
 [sng]: http://www.balabit.com/network-security/syslog-ng/opensource-logging-system
 [ivykis]: http://libivykis.sf.net/
 [rcc]: https://github.com/algernon/riemann-c-client
 [lmc]: https://github.com/algernon/libmongo-client

If everything is installed at the standard locations, getting the
Incubator to compile is as simple as this:

    $ git clone https://github.com/balabit/syslog-ng-incubator.git
    $ cd syslog-ng-incubator
    $ autoreconf -i
    $ ./configure && make && sudo make install

If anything happens to be installed in a non-standard location, one
will need to adjust `PKG_CONFIG_PATH` to help the configure script
locate the needed libraries.

Once installed (the configure script will figure out where syslog-ng
modules are, and modules will be put there), syslog-ng will
automatically recognise the new modules. One can make sure that this
is the case by running the following command after installation:

    $ syslog-ng --module-registry

## The Plugins

Now that we're over the hard part of compiling and installing the
Incubator, lets see what is inside! I will start with the easier
things, and move on to the more complicated features as we progress.
That is, we'll start with some [template functions](#bfp), have a
glance at the [trigger source](#trigger), then explore the
[rss destination](#rss), and finish off with the
[riemann destination](#riemann).

### <a name="bfp" href="#bfp">Template functions</a>

The Incubator gives us three new template functions, some less useful
than the others, and one that's a huge, ugly hack for a problem that I
ended up solving in a very different way - without the hack.

These functions are the `$(or)` template function, which takes any
number of arguments, and returns the first one that is not empty. The
main use case here is that if you have, say, similarly named fields,
but some messages have one, the others another, and you want to
normalize it, `$(or)` is one way to do it:

    $(or ${HOST} ${HOSTNAME} ${HOST_NAME} "<unknown>")

Another function is `$(//)`, which does the same thing as the built-in
`$(/)` template function: divide its arguments. Except this one works
with floating-point numbers only, while the built-in one is for
integers exclusively. Using it is simple, too:

    $(// ${some_number} 3.4)

The last template function provided by the Incubator is `$(state)`,
which can be used to maintain global state, that does not depend on
log messages. You can set values in here, like counters, from within a
template function. It is possible to count the total amount downloaded
data when processing a HTTP server log, for example. But it's slow,
and there are better ways to do the same thing, syslog-ng really isn't
the best tool for this kind of job. If anyone happens to find a
use-case for it, please let me know. As for using it, it has two
modes: set (with two arguments) and get (with one):

    $(state some-variable ${VALUE})
    $(state some-variable)

### <a name="trigger" href="#trigger">Trigger source</a>

The trigger source has many in common with the built-in mark feature:
at given intervals, it sends a message. This is mostly a debugging
aid, when you want to generate messages without an external tool. It
only has two options: `trigger-freq()` and `trigger-message()`, which
default to *10* and *"Trigger source is trigger happy."*,
respectively. It also accepts a number of common source options such
as `program-override()`, `host-override()` and `tags()`.

To use it, one just needs to set it up like any other source, and bind
it to a destination with a log statement:

    source s_trigger {
     trigger(
      program-override("trigger")
      tags("trigger-happy")
      trigger-freq(5)
      trigger-message("Beep.")
     );
    };

Without a `program-override()` option, messages will be attributed to
syslog-ng, which is likely not what you want, even while debugging.
Internal messages are usually routed somewhere else.

### <a name="rss" href="#rss">RSS destination</a>

The RSS destination is an interesting beast. It offers an Atom feed of
the last hundred messages routed to the destination. I could very well
imagine this being useful in a situation where one already has
monitoring set up to listen on various RSS sources - this would be
just another. It also works well with most RSS feed readers. The
length of the feed is not configurable at this time, and the number of
options is limited to `port()`, `title()`, `entry-title()` and
`entry-description()`.

The first one specifies which port the destination should listen on
(it serves one client at a time!); `title()` can be used to set the
title of the feed itself, while `entry-title()` and
`entry-description()` can be used with templates to fill in the
per-message Atom entries.

Once we have a suitable path we want to route to the RSS destination
(such as critical error messages only), we can set it up like this:

    destination d_rss {
     rss(
      port(8192)
      feed-title("Critical errors in the system")
      entry-title("Error from ${PROGRAM} @ ${HOST_FROM} at ${ISODATE}")
      entry-description("${MESSAGE}")
     );
    };

### <a name="riemann" href="#riemann">Riemann destination</a>

Being the original motivator for the Incubator, I left this last. This
module is the interface between your logs and the
[Riemann](http://riemann.io/) monitoring system. With this, you can
take all the legacy applications that are hard to monitor, but provide
log files, use syslog-ng's extraordinary log processing power, and
send clear and concise events over to Riemann.

One can use it to monitor logins, downloads, uploads, exceptions -
pretty much anything. Just extract some metric, or state, send it over
to Riemann, and that will do the heavy lifting. What exactly can be
done, will be worth a separate blog post, so for now, I will give just
a very tiny example:

    destination d_riemann {
     riemann(
      ttl("120")
      description("syslog-ng internal errors")
      metric(int("${SEQNUM}"))
     );
    };

Hook it up to a path that collects syslog-ng internal messages,
keeps only error messages, and routes it toward this destination:

    log {
      source { internal(); };
      filter { level(err..emerg); };
      destination(d_riemann);
    };

The destination itself has the following options:

 * `server()`: The server to connect to, defaults to *localhost*.
 * `port()`: The port the Riemann server is listening on, defaults to
   *5555*.
 * `type()`: The type of connection: *UDP* or *TCP*. Defaults to
   *TCP*.
 * `host()`: The host field of the Riemann event, defaults to
   *`${HOST}`*.
 * `service()`: The service field of the Riemann event, defaults to
   *`${PROGRAM}`*
 * `state()`: The state field of the Riemann event, without a default.
 * `description()`: The description of the event, with no default.
 * `ttl()`: The time-to-live of the event, no default.
 * `metric()`: The metric to send to Riemann. This needs to be either
   an integer or a floating point number. Using type-hinting is
   advised here. Without one, the destination will try to parse the
   value of this option as a float.
 * `tags()`: As the name implies, this adds tags to the Riemann event.
   By default, all the tags that are set on the message, will be
   forwarded.
 * `attributes()`: With this option, one can set custom attributes on
   the Riemann event. The syntax is the same as for `value-pairs()`,
   with a few enhancements. What the difference is, is left as an
   exercise to the reader: the example config that comes with the
   Incubator has a hint.

## Closing thoughts

The Incubator contains one more tool, one which can be used to
visualise logs in strange ways. But that's not strictly related to
syslog-ng, isn't a module, either, so I will not describe it here
right now. The above was quite a lot already, I believe.

As a closing thought, I would just like to say that while the
Incubator is home for experimental modules, some of them are used in
production. Don't be afraid to use them, especially when packages
start to arrive to your favourite GNU/Linux distribution!
