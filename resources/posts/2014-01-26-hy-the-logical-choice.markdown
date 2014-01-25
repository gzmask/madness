---
title: "Hy: The logical choice"
date: 2014-01-26 00:55
tags: [Technology, Hacking]
---

I would like to start this post with a **huge** thank you to Nicolas
Dandrimont and Paul Tagliamonte, for their invaluable help and
patience while I was venting, rambling and spamming the [#hy][irc:hy]
IRC channel the past few days. I also owe an apology and a thank you
for the rest of the inhabitants for the random garbage I poured onto
the channel from time to time. Reason being, that I had to get my mind
off of all the things I normally occupy myself with, so I set out on a
quest to implement [miniKanren][mk], preferably in a language I like
(no C), where a suitably complete implementation does not exist yet
(so no Clojure, either). The logical choice was, of course,
[Hy][hylang], and in this post, I will describe my journey on the
quest so far, and also explain the choice in the process.

Please be aware, that the work I'm presenting is a work in progress,
on a field I have very little prior expertise in, a field I barely
understand, if at all. It's a journey of pain and suffering, but also
a journey of great revelations, and many moments of pure joy and
clarity. Follow along, if you happen to be interested in either Logic
programming, [Hy][hylang], or if you'd simply like to follow along an
adventure into uncharted territories!

 [irc:hy]: irc://irc.freenode.net/#hy
 [mk]: http://minikanren.org/
 [hylang]: http://hylang.org/

<!-- more -->

Lets first start with a short intro to what [miniKanren][mk] is: it's
an embedded domain specific language for logic programming. That's a
handful, isn't it? So what is [logic programming][wp:lp]?

 [mk]: http://minikanren.org/
 [wp:lp]: https://en.wikipedia.org/wiki/Logic_programming

> Logic programming is a programming paradigm based on formal logic.
> Programs written in a logical programming language are sets of
> logical sentences, expressing facts and rules about some problem
> domain. Together with an inference algorithm, they form a program.
> Major logic programming languages include Prolog and Datalog.

The miniKanren DSL is a fairly small package, something that is
reasonable to understand, and there is an awesome book explaining it,
in a very, very sweet way: [The Reasoned Schemer][trs]. It has a
couple of low-level constructs, and builds pretty much everything else
on top of that. *But what is it good for? What problems does it
solve?* Those are very good questions, and let me answer them, before
we start on the journey.

 [trs]: http://mitpress.mit.edu/books/reasoned-schemer

Logic programming is very good at solving problems one can express
with logical relations, such as Einstein's famous
[Zebra puzzle][zebra], which, written in [Hy][hy], using
[adderall][hy:adderall], looks like this:

 [zebra]: https://en.wikipedia.org/wiki/Zebra_Puzzle
 [hy]: https://github.com/hylang/hy
 [hy:adderall]: https://github.com/algernon/adderall

<div class="pygmentize" data-language="clojure">
(run* [q]
 (≡ [#?_ #?_ [#?_ #?_ 'milk #?_ #?_] #?_ #?_] q)
 (firstᵒ q ['norwegian #?_ #?_ #?_ #?_])
 (nextᵒ ['norwegian #?_ #?_ #?_ #?_]
        [#?_        #?_ #?_ #?_ 'blue] q)
 (rightᵒ [#?_ #?_ #?_ #?_ 'ivory]
         [#?_ #?_ #?_ #?_ 'green]
         q)
 (memberᵒ ['englishman #?_            #?_     #?_     'red] q)
 (memberᵒ [#?_         'kools         #?_     #?_     'yellow] q)
 (memberᵒ ['spaniard   #?_            #?_     'dog    #?_] q)
 (memberᵒ [#?_         #?_            'coffee #?_     'green] q)
 (memberᵒ ['ukrainian  #?_            'tea    #?_     #?_] q)
 (memberᵒ [#?_         'lucky-strikes 'oj     #?_     #?_] q)
 (memberᵒ ['japanese   'parliaments   #?_     #?_     #?_] q)
 (memberᵒ [#?_         'oldgods       #?_     'snails #?_] q)
 (nextᵒ [#?_ #?_    #?_ 'horse #?_]
        [#?_ 'kools #?_ #?_    #?_]
        q)
 (nextᵒ [#?_ #?_            #?_ 'fox #?_]
        [#?_ 'chesterfields #?_ #?_  #?_]
        q))</div>

This isn't much longer than the puzzle's explanation itself, and
mirrors it very well, being a declarative list of logical relations
our search must satisfy. The way it works, is that we give the solver
a set of requirements, or relations, and it will go out and try to
find a solution. It really is a beautiful thing, which allows one to
reason about data in a spectacularly succinct way. For example,
there's a project built upon [adderall][hy:adderall], called
[hydiomatic][hy:hydiomatic], which is a tool that can transform Hy
code. The way Hydiomatic works is that it searches for patterns, using
relations, and will suggest better alternatives. Let me show you an
example!

 [hy:hydiomatic]: https://github.com/algernon/hydiomatic

<div class="pygmentize" data-language="clojure">
(defrules [rules/firstᵒ rules/firsto]
 [`(get ~?x 0) `(first ~?x)])</div>

What this does, is it tells Hydiomatic, that whenever it sees a form
that matches the first thing in the array (<code>(get ~?x 0)</code>),
it should replace that with the second thing (<code>(first
~?x)</code>). Internally, this rule gets translated to something very
similar to the following (where `expr` is defined externally):

<div class="pygmentize" data-language="clojure">
(run 1 [alt]
  (condᵉ
    [(fresh [?x]
       (≡ expr `(get ~?x 0))
       (≡ alt `(first ~?x)))]
    [(≡ alt expr)]))</div>

Which means, that if `expr` is a form where the first member is `get`,
the third - and last - is `0`, and the second is anything, then `alt`
will be unified with another expression, using the member extracted
via unification from the first. If `expr` does not match that form,
then we'll hit the second branch of `condᵉ`, and `alt` will become the
same as `expr`.

The beautiful thing here is, that logical relations are two-way. If one
lifts this code out into a function:

<div class="pygmentize" data-language="clojure">
(defn subst/firstᵒ [expr alt]
  (fresh [?x]
    (≡ expr `(get ~?x 0))
    (≡ alt `(first ~?x))))</div>

Then one will be able to run it both ways:

<div class="pygmentize" data-language="clojure">
(run* [q] (subst/firstᵒ `(get coll 0) q))
;=> [(first coll)]
(run* [q] (subst/firstᵒ q `(first coll)))
;=> [(get coll 0)]</div>

Now, let that sink in for a moment.

One can run things backwards. That's a **very** powerful thing,
because one can, among other things, use this to generate data for
testing, and the path of generative testing is one entertaining path,
I tell you!

But that is not all!

<div class="pygmentize" data-language="clojure">
(run* [q] (subst/firstᵒ `(get coll ~q) `(first coll)))
;=> [0]
(run* [q] (subst/firstᵒ `(get ~q 0) `(first coll)))
;=> [coll]
(run* [q] (subst/firstᵒ `(get coll 0) `(~q coll)))
;=> [first]</div>

One can ask the DSL to infer any part of the relation, and it will.
So, in short, whenever one wants to reason about data, generate data
by declaring logical steps to do so, or solve puzzles, logic
programming is the most practical weapon to wield.

The path leading here, though, has been a rough one, for a host of
reasons: even though I have been following [Hy][hylang] development
from pretty much the very beginning, I'm by no means an expert in the
language. Pythonisms surprise me every now and then, not being Clojure
likewise. I'm not a master of macros, yet, [adderall][hy:adderall]
needed plenty of macro magic to work. And I'm not all that comfortable
with python generators (I'd much prefer Clojure's LazySeq). So my
journey had a lot of cursing, and was not free of hair loss, either.

 [hylang]: http://hylang.org/

One of the first big challenges I faced, and which pretty much halted
the development for almost a week was lazyness. You see, there's this
`condᵉ` thing, which one can give a number of options to, and it will
return with all options wherein all relations succeed. Originally, it
was implemented as a function, and that worked. However, it had one
huge drawback: function arguments get evaluated before they're passed
to the function. This meant that if I had a function, that was built
on `condᵉ`, and was recursive, I ended up recursing forever, without
ever entering the function itself. It had to be turned into a macro,
that controls carefully when arguments get evaluated, and then, I was
able to proceed. For this, I had to learn macros much better, had to
understand python generators too, and annoy the good people of the
[#hy][irc:hy] IRC channel.

 [irc:hy]: irc://irc.freenode.net/#hy

But alas, I am not going to explain how [adderall][hy:adderall] works
internally, because [The Reasoned Schemer][trs] does a much better job
at it. Instead, I will tell you the reasons behind choosing Hy to
implement this in the first place.

As explained in the intro, I wanted to implement [miniKanren][mk], as
a way to better understand logic programming. I already dabbled into
the field before, but only lightly. I already knew about miniKanren,
and found it very expressive, easy to understand relations. I also
enjoyed the two books prior to [The Reasoned Schemer][trs], therefore
choosing miniKanren as something to implement was pretty much given.

The language in which to implement it, was up for debate though: I can
write a good number of languages reasonably well: *C*, *C++*, *Java*,
*Python*, *PHP*, *Perl*, *Shell*, *Emacs Lisp*, *Guile*, *JavaScript*,
*Clojure*. Of which, I refuse to write *PHP* and *Java*, *Shell* would
be a bit awkward to implement miniKanren in (though certainly
possible, and I may embark on that journey too at some point, after I
retire). I don't like *C++*, and I do *C* at my day job, and I wanted
something different to what I write eight hours a day. *JavaScript*?
Nah. *ClojureScript* then, maybe, but that already has
[core.logic](https://github.com/clojure/core.logic), just like
*Clojure*. So we're left with *Python*, *Perl*, *Emacs Lisp* and
*Guile*. *Guile* is the language I know least among these, so I
dropped that. *Emacs Lisp*? I was seriously considering that, but I
also wanted the result to not be tied to an editor. *Python* or
*Perl*. It's been years I wrote *Perl*, and I hate *Python* with a
passion... But then I remembered: there is [Hy][hylang]! It's a Lisp
on top of *Python*, with macros and everything. *Python*, too, has two
libraries that implement some of miniKanren, but both of those are
awkward to use, and are incomplete. I wanted to make one that's
powerful enough to solve real problems.

Hy made that possible. Macros made a lot of things easier, and allowed
me to build [hydiomatic][hy:hydiomatic] on top of it all too. I could
not have done all this in Python. It was Hy that gave adderall the
kick, the tools to build it with.

And with all the bits and pieces put together, there we now have
[adderall][hy:adderall], a reasonably complete [miniKanren][mk]
implementation, more complete and more usable than any of the
pure-Python implementations. We also have a static code analyser,
[hydiomatic][hy:hydiomatic] built on top of it, which will, in the
next couple of days, learn how to obfuscate Hy code, too, because
running backwards is simply amazing!

This has been my journey into logic programming so far. A journey that
is far from complete, lots of things remain to be done, lots of fun
things to write about in the future. And lots of fun things to talk
about, too, if all goes well!
