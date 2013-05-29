---
title: "Hunting Hy and low"
date: 2013-04-22 14:00
tags: [Technology, .planet.debian]
---

I hate Python with a passion, it is a terrible language, and I find it
painful to write significant amounts of code in it. It irks me, it
makes me upset, slows me down, and there we have a vicious circle.
Unfortunately, I do have to work with much more python code than I
would like to. I have to suffer through the insanity of significant
whitespace, wade through empty <kbd>\_\_init\_\_.py</kbd> files, get
bogged down by a Giant Interpreter Lock, or tear my hair out when I
encounter yet another major difference between python2 and python3, in
a codebase that is supposed to be usable by both. And then we have
decorators, and all kinds of silly workarounds. I hate python, but I
have to work with it.

Thankfully, there is one project that does a particularly good job at
saving the day, by letting me keep my sanity, while still allowing me
to pretend that I write python, so others whom I work with won't
suspect a thing. This silver bullet-like thing is called [Hy][hy], and
it is made of pure awesomeness.

 [hy]: http://hylang.org/

<!-- more -->

Let me clarify something further: I hate most languages for one reason
or the other, but there's a few I hate considerably less than the
others. Typically, lisp-y languages fall into the less-hate bucket. I
will not go into detail why I prefer lisp, the same reasons have been
said many times over the decades by people far more knowledgeable than
I am, so I will keep my reasons short: **lisp is powerful**. And
[Hy][hy] inherits some of that power (an increasing amount of it, from
multiple lisp dialects - the best of all worlds!), yet, compiles down
to Python bytecode, and inter-operates with Python smoothly. So much
so, that if I write a piece of code in Hy, it can be used from within
Python, and it will neither look, nor feel alien to the environment.
It is indistinguishable from python, as far as using the results go.
No Hy core libraries needed, no abstractions leak through (well,
perhaps apart from keywords and maybe a few other things, but those
are easy to pay attention to when one wants to interface with python
without surprising anybody), it just works.

 [hy]: http://hylang.org/

And this is one of the most fundamental reasons I like Hy, and why I
contribute to it, in my small way. Because it makes python bearable,
allows me to hide its warts, and work in an environment that is much
more comfortable.

Lets take a trivial example: the fizzbuzz kata, slightly complicated
further, so it reads: <em>"For each number between 1 and 105
(inclusive), print out 'fizz' if the number is a multiple of three, or
if three appears in its digits. Likewise, print out 'buzz' for
multiples of five, or when five appears in the number's digits. And
finally, do the same for seven, and print out 'beep' in those cases.
For all others, print the number itself."</em>

<div class="row-fluid">
<div class="pygmentize span6" data-language="clojure">(import [functools [partial]])

(defn multiple-of? [n x]
  (not (% n x)))

(defn contains-number? [n x]
  (-> (str x) (in (str n))))

(defn fizzbuzz-constraints [constraints n x]
  (any (map (lambda [f] (f n x)) constraints)))

(defn match [n constraints factors]
  (->> factors
       (filter (partial fizzbuzz-constraints constraints n))
       (list)))

(defn fizzbuzz [constraints targets n]
  (let [[factors (-> targets .keys sorted)]
        [matches (match n constraints factors)]]
    (if (not matches)
      (str n)
      (->> matches
           (map (lambda [x] (get targets x)))
           (.join "")))))

(if (= __name__ "__main__")
  (do
    (def *factors* {3 "fizz" 5 "buzz" 7 "beep"})
    (def *constraints* [multiple-of? contains-number?])

    (print (.join ", " (map (partial fizzbuzz *constraints* *factors*) (range 1 106))))))
</div>

<div class="pygmentize span6" data-language="python">
from functools import partial

def is_multiple_of(n, x):
    return not (n % x)

def does_contain_number(n, x):
    return (str(x) in str(n))

def fizzbuzz_constraints(constraints, n, x):
    return any(map((lambda f: f(n, x)), constraints))

def match(n, constraints, factors):
    return list(filter(partial(fizzbuzz_constraints, constraints, n), factors))

def fizzbuzz(constraints, targets, n):
    factors = sorted(targets.keys())
    matches = match(n, constraints, factors)
    if not matches:
        return str(n)
    else:
        return "".join(map((lambda x: targets[x]), matches))

if __name__ == '__main__':
    FACTORS = {3: 'fizz', 5: 'buzz', 7: 'beep'}
    CONSTRAINTS = [is_multiple_of, does_contain_number]
    print ', '.join(map(partial(fizzbuzz, CONSTRAINTS, FACTORS), range(1, 106)))


</div>
</div>

A few things of note: the [Hy][hy] variant has better function names,
because the language allows '<kbd>?</kbd>' and '<kbd>-</kbd>' in
symbol names. While it has more lines, thanks to the threading macros
(<kbd>-&gt;</kbd> and <kbd>-&gt;&gt;</kbd>), it becomes easier to
express the flow of data. Not to mention, that we got rid of
significant whitespace, indentation becomes much easier (there is no
guessing involved!), yet, the resulting bytecode is exactly the same.

Once the language has some more syntactic sugar, we'll be able to make
the Hy variant even neater. This is another strong point for Hy: we
can make it better! We can make it convenient! While sacrificing
nearly nothing.

[Hy][hy] truly is amazing.
