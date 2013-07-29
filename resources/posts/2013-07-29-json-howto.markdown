---
title: "HOWTO: JSON processing with syslog-ng"
date: 2013-07-29 18:15
tags: [Technology, HOWTO, syslog-ng, .planet.debian]
---

Continuing my [HOWTO series][tags/howto], in this article, I will
explore how to set up syslog-ng to process JSON in various scenarios.
We will touch the subject of interoperability, and have a look at
multiple scenarios, in the hope that readers will find something
similar to their own needs. There are many different ways in which one
can plug JSON into their logging system, and it is very likely that I
will not be able to cover it all within this HOWTO. However, I will
explain the basics, and how to plug the pieces together, which should
at least be a useful foundation to build even more complex systems onto.

 [tags/howto]: /blog/tags/howto/

In essence, we will look at the following scenarios:
[raw JSON transport][howto:raw-json],
[JSON over syslog][howto:json-syslog],
[JSON and syslog payload in the same stream][howto:mixed-stream]. The
documentation will cover both syslog-ng 3.4 and 3.5, where the 3.5
parts will be clearly marked as such. The 3.3 series do not have a
JSON parser, and as such, is insufficient for most needs explored in
this guide.

 [howto:raw-json]: /blog/2013/07/29/json-howto/#howto-raw-json
 [howto:json-syslog]: /blog/2013/07/29/json-howto/#howto-json-syslog
 [howto:mixed-stream]: /blog/2013/07/29/json-howto/#howto-mixed-stream

<!-- more -->

In the default setting, the JSON parser will parse the
<strong>MESSAGE</strong> part of an incoming log event, and place
every key into a macro of the same name. If the incoming JSON is
structured, it the keys will be converted to dot notation format:

<div class="pygmentize" data-language="javascript">{"deeper": {"level": "very much so"}}</div>

In this case, one can access the value as <code>${deeper.level}</code>
within a syslog-ng template.

The parsed keys can be prefixed using the <code>prefix()</code> option
of the json-parser. One can also change what the parser will receive
as input by using the <code>template()</code> option.

See the [documentation][docs:syslog-ng-json] for more information on
these.

 [docs:syslog-ng-json]: http://www.balabit.com/sites/default/files/documents/syslog-ng-ose-3.4-guides/en/syslog-ng-ose-v3.4-guide-admin/html/json-parser.html

### <a name="howto-raw-json">Transporting raw JSON</a>

The simplest scenario is to take a transport like TCP, and simply push
raw JSON structures through, one each line, and have syslog-ng parse
that to its internal representation. One can do all kinds of things
with the data then: transport it further as JSON, store it in files,
in a database - the possibilities are endless, but we will explore two
scenarios.

#### JSON on TCP, using syslog field names

The first one is receiving JSON on TCP, using syslog field names,
store the parsed stuff in a file (using the conventional format), and
in mongodb.

<pre>source s_tcp_json { tcp(port(10514) flags(no-parse)); };
parser p_json { json-parser(); };
destination d_file { file("/var/log/remote.log"); };
destination d_mongodb { mongodb(collection("remote_log")); };
log {
 source(s_tcp_json);
 parser(p_json);
 destination(d_file);
 destination(d_mongodb);
};</pre>

This needs a bit of an explanation: we use the **no-parse** flag on
the source, because by default, the *tcp()* source expects a message
that conforms to the old syslog protocol. We have raw JSON, and we
need to tell the source that. The other elements should be self
explanatory, I believe, with the exception of *json-parser()*: we give
it no parameters, so it will not modify the keys in the JSON it
receives, keeping them intact, as-is.

For this reason, incoming messages need to have the same keys as
syslog-ng normally would use internally: DATE, HOST, MESSAGE, and so
on, like in the following example:

<div class="pygmentize" data-language="javascript">{"PROGRAM":"prg00000","PRIORITY":"info","PID":"1234","MESSAGE":"seq: 0000000000, thread: 0000, runid: 1374490607, stamp: 2013-07-22T12:56:47 MESSAGE...","HOST":"localhost","FACILITY":"auth","DATE":"Jul 22 12:56:47"}</div>

#### JSON on TCP, custom field names

A more likely scenario is when we receive JSON, but not in a layout
syslog-ng uses internally. In this case, we need to rewrite the
message a little, and possibly use templates, depending on what kind
of output we wish to use.

Lets assume the incoming JSON has these fields:

<div class="pygmentize" data-language="javascript">{"app":"prg00000","prio":"info","id":"1234","msg":"seq: 0000000000, thread: 0000, runid: 1374490607, stamp: 2013-07-22T12:56:47 MESSAGE...","source":"localhost","timestamp":"Jul 22 12:56:47"}</div>

As you can see, this has similar fields, but not quite, and *FACILITY*
is missing too.

In this case, we'll use a different configuration:

<pre>source s_tcp_json { tcp(port(10514) flags(no-parse)); };
parser p_json { json-parser(prefix(".json.")); };
destination d_file { file("/var/log/remote.log"
                          template("${.json.timestamp} ${.json.source} ${.json.app}[${.json.id}]: ${.json.msg}\n")); };
destination d_mongodb { mongodb(collection("remote_log")
                                value-pairs(
                                  pair("PROGRAM" "${.json.app}")
                                  pair("HOST" "${.json.source}")
                                  pair("PID" "${.json.id}")
                                  pair("MESSAGE" "${.json.msg}")
                                  pair("DATE" "${.json.timestamp}")
                                  pair("PRIORITY" "${.json.prio}")
                                  pair("FACILITY" "auth")
                                )); };
log {
 source(s_tcp_json);
 parser(p_json);
 destination(d_file);
 destination(d_mongodb);
};
</pre>

We use the **prefix()** option of *json-parser()*, to avoid collisions
with the default syslog-ng namespace. Then, we use a template for the
file destination, and *value-pairs()* for the MongoDB one. Doing all
this, we get similar results as we did in the first scenario.

### <a name="howto-json-syslog">JSON over syslog</a>

An even likelier scenario is when we have JSON payload in the message
part of a normal syslog message. This is useful because the logs
themselves remain syslog compatible, and can be transported through
devices that know no better.

Thankfully, if we do not tell syslog-ng that we do not want any
parsing on the input, the json-parser will receive the MESSAGE part of
the log, so if we have JSON payload there, things will just work:

<pre>source s_tcp_json_payload { tcp(); };
parser p_json { json-parser(); };
destination d_file { file("/var/log/remote.log"); };
destination d_mongodb { mongodb(collection("remote_log")); };
log {
 source(s_tcp_json_payload);
 parser(p_json);
 destination(d_file);
 destination(d_mongodb);
};</pre>

Again, this assumes that the JSON payloads uses field names compatible
with syslog-ng's defaults. Adapting it to use templates or value-pairs
can of course be made, as shown in an example above.

As an example, here's how a syslog message with JSON payload would
look like:

<pre>&lt;38&gt;2013-07-22T13:45:54 localhost prg00000[1234]: {"MESSAGE": "foo", "PROGRAM": "bar"}</pre>

This would, of course, override the MESSAGE and PROGRAM fields, but
the rest would be kept intact.

### <a name="howto-mixed-stream">Mixed JSON and syslog stream</a>

In order to parse JSON that is intermingled with normal syslog
streams, we can use a new feature of syslog-ng 3.4: junctions. These
allow us to create branches, and tell syslog-ng something along these
lines:

<blockquote>If there's an incoming message, that begins with
<code>@json:</code>, parse it as JSON, otherwise treat it as a normal
syslog message.</blockquote>

We can, of course, skip the part that mandates an in-stream marker,
and things will just work, but in that case, for each JSON message, an
error will get logged too, which is undesirable. Therefore, if one
wants to intermingle JSON and syslog traffic on the same wire, it's
best to do so with the JSON having a marker prefix. The marker,
however, is not going to be part of the string the json parser
receives, so setting it to an opening curly bracket will not work,
unless the template is modified accordingly.

The config to do this looks like this:

<pre>block parser mixed-json-parser() {
 channel {
  junction {
   channel {
    parser { json-parser(marker("@json:")); };
    rewrite { set-tag(".json"); };
    flags(final);
   };
   channel {
    flags(final);
   };
  };
 };
};

source s_mixed {
 tcp();
 parser { mixed-json-parser(); };
};</pre>

A more elaborate example can be found in a
[post by Bazsi][post:bazsi-cee].

 [post:bazsi-cee]: https://bazsi.blogs.balabit.com/2012/05/cee-prototype-and-a-show-case-for-the-new-3-4-features/

### Closing thoughts

That covers most of the use cases I saw for the JSON parser, if there
are cases uncovered, or questions unanswered, I can be reached via
[twitter][social:twitter], [email][social:work] and a whole host of
other ways, and I will happily expand this tutorial if there's enough
demand for more.

 [social:twitter]: https://twitter.com/algernoone
 [social:work]: mailto:algernon@balabit.hu
