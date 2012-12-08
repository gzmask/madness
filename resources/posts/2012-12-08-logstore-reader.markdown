---
title: LogStore news
date: 2012-12-08 23:15
tags: [Technology, Clojure, Hacking, syslog-ng]
comments: 1
---

It has been a while ago that I last worked on the
[LogStore reader API][1], more than half a year. But the long delay
has not been in vain, for I have learned a lot during this
time. Today, being in a foul mood, I sat down to work on the library
to cheer myself up.

 [1]: //asylum.madhouse-project.org/blog/2012/04/30/logstore-reader/

<!-- more -->

There were numerous mistakes I made in the first version of the
library, which made it very, very hard to resume working on
it. Namely, it was written with objects in mind, and too much effort
was placed on being *efficient* (though, I failed at that badly).

Another problem was using [gloss][2], which is a great library, but it
is an overkill for the task at hand (since I only need reading, not
writing).

 [2]: https://github.com/ztellman/gloss
 
To remedy the situation, I wrote my own [binary parsing DSL][3]
library, jokingly called *balabit.blobbity*, which does only what I
need and nothing more, in a way that fits the new version of the
LogStore reader better.

 [3]: https://github.com/algernon/balabit.blobbity

Furthermore, I completely rethought how the processed LogStore will be
represented. In the original design, I used special types and records
to add some structure to it, in the new one, I use a simple map. And
while in the original, the low-level details were exposed in the
public API (think <code>logstore/nth-record</code> and similar
functions), in the new one, they aren't. It is all a single map,
neatly structured into logical sections.

And the best part? It is lazy. Only as much of the store gets read as
is necessary, and no more. Neither time nor memory is wasted by
setting up an index when the logstore is opened.

As an example, lets compare the implementation of a function that
prints a random message! The old way can be seen in
[this example here][4], it is fairly complicated, one has to select a
random record, and a random message within it too (and it can fail
too, if the random record does not happen to be a chunk!). So how does
the same look with the new API?

<pre class="prettyprint lang-clj">(defn random-message-printer
  ([] (random-message-printer "logstores/loggen.compressed.store"))
  ([filename]
     (rand-nth (logstore/messages (logstore/from-file filename)))))</pre>

 [4]: https://github.com/algernon/balabit.logstore/blob/master/src/balabit/logstore/examples.clj#L54-L72

Much more readable, far less low-level hackery is needed, and it is
also more idiomatic.

The rewrite is far from complete, serialized messages are a work in
progress (compared to the old library, the new one can't read tags
yet, and a few things aren't decoded into human-readable form yet),
and there still is no Java API. However, the new structure also has
the benefit of being easier to interface it from Java, so once I'm
done with the Clojure side, Java is next.

It might take another few months until this gets to a usable state, as
my time to work on the library is **very** limited, but I'm reasonably
sure that I will not need to rewrite it again, and that the current
design is good.
