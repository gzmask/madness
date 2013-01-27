---
title: "Make your servers tweet harder"
date: 2013-01-25 18:00
tags: [Technology, syslog-ng]
---

A couple of years ago, [Peter Gyongyosi][gyp] wrote a piece titled
[Make your servers tweet][servertweet], wherein he demonstrated a
syslog-ng destination that can send logs to [twitter][twitter]. The
code has been bit-rotting since, yet, the crazy value of the idea
remained, so today, driven by an urge to do something silly, I
assembled a [tool][log-tweeter] to make my servers tweet, harder.

 [gyp]: http://gyp.blogs.balabit.com/
 [servertweet]: http://gyp.blogs.balabit.com/2009/09/make-your-servers-tweet/
 [twitter]: https://twitter.com/
 [log-tweeter]: https://github.com/algernon/log-tweeter
 
<!-- more -->

Unlike Peter's solution, mine is not a [syslog-ng][sng] patch, nor a
module, but a tiny python script, and a carefully crafted
config. While both are available on [GitHub][log-tweeter], I will
present both the code and the configuration here.

  [log-tweeter]: https://github.com/algernon/log-tweeter

To be able to tweet, we need three things: we need a program, a
twitter account, and permissons for the program to post on our
behalf. Registering a twitter account is left as an exercise for the
reader, but registering an application, and giving it permissions is a
bit more involved, so we'll go through that. But first, lets code!

<div class="pygmentize" data-language="python">#! /usr/bin/python
import twitter
import json
import os
import sys

def tweet(message):
	tweet = ""
	if len(message) &gt; 125:
		tweet = "%s... #syslog_ng" % message[:125]
	else:
		tweet = "%s #syslog_ng" % message

	cfg = json.load(open("/etc/syslog-ng/tweeter.credentials.json", 'r'))
	t = twitter.Api(
		consumer_key = cfg['consumer_key'],
		consumer_secret = cfg['consumer_secret'],
		access_token_key = cfg['access_token_key'],
		access_token_secret = cfg['access_token_secret']
	)
	
	t.PostUpdates(tweet)

while True:
	message = sys.stdin.readline().strip()
	if message:
		tweet(message)
	else:
		sys.exit(0)</div>

This simple program will read credentials from
<kbd>/etc/syslog-ng/tweeter.credentials.json</kbd>, and post whatever
it receives on its standard input to Twitter. That is all. Well, not
exactly: it also hashtags everything with <kbd>#syslog_ng</kbd> and
trims long messages, but you get the idea!

Now, on to the harder part: granting access to this application. First
of all, visit the [twitter development center][dev-twitter], and log
in, then [create a new application][dev-tw-app]. Here, fill out the
values as you feel appropriate. I suggest using
<kbd>*yourname*-log-tweeter</kbd> as the application name,
<kbd>https://github.com/algernon/log-tweeter</kbd> as the website, and
the description is up to you.

 [dev-twitter]: https://dev.twitter.com/
 [dev-tw-app]: https://dev.twitter.com/apps/new

Once this is done, visit your application, and click on the
**Settings** tab, and change the application type to **Read and
Write**. When done, switch back to the **Details** page, and press the
"**Create access tokens**"" button at the bottom.

With these, you have all the information needed, so lets create a
configuration file for the log-tweeter application! All we need to do,
is paste the following JSON into
<kbd>/etc/syslog-ng/tweeter.credentials.json</kbd>, and fill in the
appropriate values from the page you still have open:

<div class="pygmentize" data-language="javascript">{
 "consumer_key": "",
 "consumer_secret": "",
 "access_token_key": "",
 "access_token_secret": ""
}</div>

You can test the application by running the <kbd>echo test |
/usr/local/bin/tweeter</kbd> command (obviously substituting the path
to wherever you placed the script). If all goes well, this should post
a tweet on your behalf.

All that is left to do, is to wire it up with [syslog-ng][sng]. We
don't want all messages to end up on twitter, that would be bad. So I
present a snippet that posts on twitter whenever I unlock my screen at
work:

<pre>
filter f_unlock {
	program("gnome-screensaver-dialog") and match("authenticated as") and match("pam_krb5");
};
rewrite r_unlock {
	set("$MESSAGE", value("LOGIN_MSG"));
	subst("pam_krb5.*: ", "", value("LOGIN_MSG"));
};
destination d_tweeter {
	program("/usr/local/bin/tweeter" template("${LOGIN_MSG}\n"));
};
log {
	source(s_src);
	filter(f_unlock);
	rewrite(r_unlock);
	destination(d_tweeter);
};
</pre>

Assuming a default Debian configuration, putting this into a file
named - say - <kbd>99-twitter.conf</kbd> under
<kbd>/etc/syslog-ng/conf.d/</kbd> and restarting [syslog-ng][sng] will
do the right thing.

 [sng]: https://www.balabit.com/network-security/syslog-ng/opensource-logging-system/overview

As an example, see [@a_syslog][a_syslog] on twitter!

 [a_syslog]: https://twitter.com/a_syslog
