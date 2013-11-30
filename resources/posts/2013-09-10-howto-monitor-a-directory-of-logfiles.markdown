---
title: "How to monitor a directory of logfiles?"
date: 2013-09-10 16:00
tags: [Technology, syslog-ng, HOWTO, .planet.debian]
---

One feature of [syslog-ng PE][sng:PE] is support for wildcard file
sources, meaning you can give it a wildcard, like
<code>/var/log/apache/*.log</code>, and it will automatically notice
new files being created or old ones disappearing. This is a very
useful feature which has been missing from the
[open source edition][sng:ose], and one that is
[being worked on][sng:ose-wcs], but is far from complete, and will not
make it into [syslog-ng 3.5][sng:3.5]. However, at least on GNU/Linux,
there is a way to implement something similar, although in a fairly
crude manner. Yet, it works surprisingly well in most cases. You only
need to abuse a few things...

 [sng:PE]: https://www.balabit.com/network-security/syslog-ng/central-syslog-server
 [sng:ose]: https://www.balabit.com/network-security/syslog-ng/opensource-logging-system/
 [sng:ose-wcs]: https://github.com/balabit/syslog-ng-3.5/tree/wild-card-file-source
 [sng:3.5]: https://github.com/balabit/syslog-ng-3.5

The trick itself is pretty easy, and is based on the same underlying
kernel feature that [syslog-ng PE][sng:PE] uses under the hood:
<code>inotify(7)</code>. What we will do, is use [incron][incron] to
monitor a directory, and create syslog-ng config file snippets
whenever a file creation event occurs. We also set up syslog-ng to
include these snippets, and reload the configuration after each event
received. This way, we immediately notice newly created logfiles
without the need to poll the directory, and with config snippets
created on the fly, we do not modify the main syslog-ng configuration
file, either.

 [sng:PE]: https://www.balabit.com/network-security/syslog-ng/central-syslog-server
 [incron]: http://inotify.aiken.cz/?section=incron&page=about&lang=en

<!-- more -->

To do this, we first need to create an appropriate syslog-ng
configuration:

<pre>@version: 3.5

destination d_all_logs {
  file("/var/log/all.log");
};

include "/etc/syslog-ng/conf.d/notify-*.conf";
</pre>

The config itself is a dummy: it has no sources, no logpaths, just a
destination, which we will use in the snippets. Next, we create the
script that will create these for us! It takes a single command-line
argument: the filename of the newly created logfile.

<div class="pygmentize" data-language="sh">#! /bin/sh
set -e

CONFDIR="/etc/syslog-ng/conf.d"
SNGCTL=/usr/sbin/syslog-ng-ctl

id="$(uuidgen | tr '-' '_')"
conffn="${CONFDIR}/notify-${id}.conf"
fn="$1"

cat &gt;${conffn} &lt;&lt;EOF
source s_${id} {
 file("${fn}");
};

log {
 source(s_${id});
 destination(d_all_logs);
};
EOF

${SNGCTL} reload</div>

This creates the config snippet with a unique filename and source
name, adds a new logpath that binds it to the <code>d_all_logs</code>
destination defined in the main configuration file, and reloads the
configuration afterwards.

Now, all we need is to tell <code>incron</code> to call this script.
Assuming we saved it as
<code>/usr/local/sbin/syslog-ng-wildcard-notify</code>, this is how
the incrontab entry would look like:

<pre>/var/log/remote.d/*.log  IN_CREATE,IN_MOVED_TO /usr/local/sbin/syslog-ng-wildcard-notify $@/$#</pre>

And that's about it! Now whenever a new file gets dropped into the
directory, syslog-ng will get notified. There is a downside, however:
when a file gets deleted, the configuration is not removed. That part
is not so simple, though: we want syslog-ng to finish reading the file
before reloading the config, so that makes it a bit trickier. But if
we don't care about that, we could just add an <code>IN_DELETE</code>
event, and write another script that looks up which snippet is
responsible for the file, delete it and reload. Doing that is left as
an exercise for the reader!
