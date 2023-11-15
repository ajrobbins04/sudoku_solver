# Overview

I've always been interested in learning a functional programming language, so I decided
it was finally time to take the leap and see what I've been missing out on! Clojure
felt like the right choice for me because I've heard that Clojure great benefits when used
to develop web applications, which is another field I'm interested in.

I created a sudoku solver that is not complete, but I'm off to a really great start on it.
I applied the sudoku solving explained by Peter Norvig, which can be found 
[here](https://norvig.com/sudoku.html) which applies constraint propagation, which is
essentially a process of elimination with two specific rules:

Rule 1: If a square has one possible value, then remove that value from its peers.

Rule 2: If a unit has only one possible place, for a value, then put the value there.

The program is able to apply these rules by utilizing 3 main data structures:
* Square - A vector that contain every square on the sudoku grid.
* Possible Values - A map that contains every square as a key and its set of possible values.
* Peers - A map that contains the squares as keys, and its value is a vector that contains

I wrote this software because there are some programs that I've written using imperative 
programming that dealt with a lot of data, and they felt cumbersome and unnecessarily complex. 
These programs would have been more successful if they were written using Clojure,
so I would love to have that option whenever I start another project where an object-oriented
approach isn't the most ideal.


*[Sudoku Solver: Part 1 (6:24)](https://youtu.be/uVps3LiKvTw)
*[Sudoku Solver: Part 2 (3:53)](https://youtu.be/GrnOPjG7hks)

### Note
There was a glitch on Screencast-O-Matic that ended the recording early. But, it ended it at the perfect time since everything in part 2 is a work in progress, while the content in part 1 is
complete (although I'm sure I can find ways to improve it).

# Development Environment

This program was written in Clojure using the Leiningen build tool. It does not import any Java classes, and it only uses built-in and user-defined functions.

* Clojure version 1.11.1
* Leiningen 2.10.0 
* Java 21.0.1 OpenJDK 64-Bit Server VM

# Useful Websites

{Make a list of websites that you found helpful in this project}
* [Clojure for the Brave and True](https://www.braveclojure.com/clojure-for-the-brave-and-true/)
* [Clojure By Example](https://kimh.github.io/clojure-by-example/#sets)
* [On The Code Again Youtube Series](https://www.youtube.com/@onthecodeagain)
* [Clojure.org Cheat Sheet](https://clojure.org/api/cheatsheet)
* [Clojure.org Reference Page](https://clojure.org/reference/reader)

# Acknowledgements
* Special thanks to Peter Norvig for explaining the best [sudoku-solving logic](https://norvig.com/sudoku.html) and to Justin Kramer for sharing his [sudoku solver in Clojure](https://jkkramer.wordpress.com/2011/03/29/clojure-python-side-by-side/) so I could learn from his work and utilize his reduce-true method.

# Future Work

* The eliminate method needs to be completed. 
* The search method needs to be completed. 
* The user should be able to enter their own sudoku puzzles to solve,
instead of relying on one hard-coded puzzle already in the program.
* Once the program is working, a timer should be added to compare the
solve times. It would be interesting to se the time difference between
easy sudoku puzzles versus difficult ones.