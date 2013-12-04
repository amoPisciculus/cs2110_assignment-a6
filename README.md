# Danaus #

## Index ##
* __bin__ compiled java .class files
* __doc__ project documentation
* __res__ resources
* __src__ source code

## Running From Command Line ##
If you prefer running danaus from the command line, as opposed to in Eclipse,
the following tips and tricks can make your life easier. We assume you're
running bash. If you prefer another shell, or are using Windows, mostly
everything can still be accomplished with Eclipse. 

Danaus has man pages located in doc/man. You can add Danaus' man pages to your
man path by adding the following to you .bashrc:

`export MANPATH=$MANPATH:$PATH-TO-DANAUS/doc/man`   

Depending on your default MANPATH, you may have to modify the command. Now, you
can type 

`man danaus`

into your terminal.

Typing java danaus.Simulator takes a lot of character strokes, and navigating
to Danaus' bin directory can be tedious. Add the following alias to your
.bashrc to make life easier:

`alias danaus="java -cp $PATH-TO-DANAUS/bin danaus.Simulator"`    

Now you can run danaus from anywhere.

## Generating javadoc ##
Danaus.jar does not come with precompiled javadoc documentation. If you would
like to generate javadoc for Danaus, navigate to **/doc** and and run

`make`

or

`make doc`

to build javadoc documentation for public classes, functions, etc. If you would
like to generate javadoc documentation for private entities as well, run

`make private`

If you are running Windows, you may have to manually enter in the commands. You
can read the Makefile to see which commands to run. After the documentation has
been generated, you can view it by opening index.html in your preferred web
browser.

In order to clean up the documentation and remove it from your file system, run

`make clean`

## Getting Help ##
Having trouble with Danaus? Get some help. Maybe your friend has a better
understanding of what to do. CS 2110 has a dream team of professors, TA's, and
consultants. Post a question on Piazza. 

## Credits ##
Many thanks to those that helped provide Danaus' art assests.

Flower sprites were taken, with permission, from
[neorice](http://neoriceisgood.deviantart.com/art/100-Flower-Sprites-348880673).

Butterfly sprites were taken, with permission, from
[David Nyari](http://toadstone.tumblr.com/).
