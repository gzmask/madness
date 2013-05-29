---
title: An Emacs well trained
date: 2013-01-20 17:00
tags: [Technology, Hacking, Video, .planet.debian]
---

Inspired by a [recent post from Athos][1], I recorded a short video
too, to see how well my environment fares. My environment and my
priorities are much different than his, though, so I'd like to quickly
describe how it was all set up.

 [1]: http://athos.blogs.balabit.com/2013/01/my-programming-environment/

<!-- more -->

## In the beginning...

In the beginning, my development machine used to have about 16Mb of
memory, and not even a gigabyte of hard disk space. I forgot the exact
specs by now, but it was quite an ancient machine even back then, at
the end of the past millennia. I used to work on the console, at 80x25
for long-long years, and even when I switched to X, I initially used
window managers like [ratpoison][rp] that mimicked how my text
terminal worked.

 [rp]: http://ratpoison.nongnu.org

Even to this day, I prefer all my windows maximised, no borders or
title bars, no panels (unless they can auto-hide), and no other
distractions, either.

Another reason for my choice of ratpoison was the mouse. I don't like
it, because I'm terribly clumsy, and my coordination is beyond
awful. Therefore, whenever I can, I will use the keyboard.

## ...came enlightenment

And use of the keyboard lead me to a tough choice right in the
beginning: [emacs][emacs] vs [vim][vim]. I tried both, but I have
never used a modal editor before, so vim fell out pretty quickly, and
I've been using emacs ever since, each year discovering more and more
things I can do without leaving it.

 [emacs]: http://gnu.org/software/emacs/
 [vim]: http://www.vim.org/

## The requirements

What I require from my desktop environment (and my editor) is to stay
out of my way, yet, be extensible enough so that I can do everything I
find a need for. I don't really care how my resources it uses, as long
as I can get my stuff done.

For these reasons, I use [GNOME 3][gnome3], with a couple of
extensions, such as [maximus][g3-maximus],
[message notifier][g3-msgnotify], and [panel settings][g3-ps]. I have
the panel auto-hidden, and my maximised windows have neither border,
nor title bars. Just perfect. It also provides me a way to easily
switch between virtual desktops using keyboard shortcuts, to launch
applications, and a whole lot of other things I can do without
touching the mouse. And it comes by default with the distribution of
my choice, too!

 [gnome3]: https://www.gnome.org/gnome-3/
 [g3-maximus]: https://extensions.gnome.org/extension/354/maximus/
 [g3-msgnotify]: https://extensions.gnome.org/extension/150/message-notifier/
 [g3-ps]: https://extensions.gnome.org/extension/208/panel-settings/

My requirements for the editor are similarly simple: it must integrate
well with the tools I use. Including a shell, git, various build
systems, and all that buzz. Proper syntax highlighting, code
navigation, on-the-fly documentation and extensibility are a must
aswell. Emacs does them all. While I will not share my
<kbd>~/.emacs</kbd>, because it's one big monolithic mess, I highly
recommend [emacs live][elive] to anyone interested in a superb starter
package.

 [elive]: http://overtone.github.com/emacs-live/

## A Demo

To show how it all fits together, I put together a short video of
implementing the [FizzBuzz][fizbuzz] test. It doesn't really show
*much*, most of the interesting things can't be seen, so let me
highlight them! Do start watching the video until then, though!

 [fizbuzz]: http://rosettacode.org/wiki/FizzBuzz

<iframe width="640" height="360" frameborder="0" allowfullscreen
        src="http://www.youtube-nocookie.com/embed/iazvlNYjtaM?rel=0"></iframe>

The first thing I do, is **<kbd>M-x eshell</kbd>**, to quickly launch
a shell from Emacs itself. It is not a separate process, but a shell
implemented in emacs itself - perfect for a few quick commands.

Next, when the project is started, I use *dired-mode* to delete files,
then launch a REPL (using [nrepl-mode][nrepl]), while I commit the
initial state via [magit][magit]'s **<kbd>C-x g</kbd>**. The REPL will
not be directly used, it is just needed so I can run code in it.

 [nrepl]: https://github.com/kingtim/nrepl.el
 [magit]: http://magit.github.com/magit/

Once these are started up, it's time to code! I visit the main source
file, <kbd>src/fizbuzz_clj/core.clj</kbd>, and evaluate both forms
using **<kbd>C-M-x e</kbd>** (evaluate the form at point), then switch
to the test cases belonging to this file with **<kbd>C-c t</kbd>**,
and start hacking.

As soon as I have a test, I run the whole test suite with **<kbd>C-c
,</kbd>**, and observe where it fails, switch back to the source,
edit, and repeat. That's about it. There were a few more shortcuts
used, like **<kbd>C-x e</kbd>** to evaluate the form before the cursor
(as opposed to **<kbd>C-M-x e</kbd>**, which evaluates the complete
form the cursor is in or after).

All through the session, I never once had to leave the comfort of my
editor, and everything I used is well integrated, with easy and
straightforward shortcuts available where appropriate.

This very same environment runs on my desktop at home and at work, on
my laptop, and even on my server (which is a 10 year old
PowerMac). Though, on the server, there's no X, but that's no problem,
screen and Emacs work just fine on the console too.

Finally, for the fun of it, I recorded another video of yours truly
writing the FizzBuzz test in C instead:

<iframe width="640" height="360" frameborder="0" allowfullscreen
        src="http://www.youtube-nocookie.com/embed/wABRKYxV0Hg?rel=0"></iframe>

This shows how I run tests against C code, and how I check manual
pages too.
