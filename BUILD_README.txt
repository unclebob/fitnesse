NOTES FOR DEVELOPERS
====================

This document is intended for those who wish to build FitNesse and do
development work.


Building
========

The build.xml and a proper internet connection is sufficient to build FitNesse.
The build process will bootstrap itself by downloading Ivy (dependency management) and from there will download the modules required to build and test FitNesse.

To build and run all tests, run the command

  $ ant
 
which builds the 'all' target. 


Testing
=======

To run the unit tests:

  $ ant unit_test

There is a second source directory, "srcFitServerTests", which contains units
tests that test invocation of Fit servers written in Ruby, C++, and .NET. These
tests are not run as part of the normal ant test-related targets. When using an
IDE, make sure it does not invoke these tests when running the "normal" tests
under the "src" directory.

Direct any questions to the FitNesse yahoo group or to unclebob@cleancoder.com.


Working with Eclipse and IntelliJ
=================================

There are a few things to keep in mind when working from an IDE:

1. The ant build generates two files from the templates in the "templates"
   directory:
   * FrontPage.content.txt.template is used to generate
     FitNesseRoot/FrontPage/context.txt;
   * FitNesseVersion.java.template is used to generate
     src/fitnesse/FitNesseVersion.java.

   You can execute

     $ ant all

   to generate those files.

2. Apache Ivy is used for dependency management. You can either install an Ivy
   plugin in your preferred IDE or run

     $ ant retrieve

   that will download the dependencies and copy them to lib/, from where your
   IDE can pick them up.


.NET Support (8/6/2008)
=======================

We re-installed the dotnet/*.dll and dotnet/*.exe files, taking them from the
"fitnessedotnet" release on Sourceforge. This will allow the .NET Acceptance
Tests to run right out of this distribution. However, you should consider using
"fitnessedotnet". See the page FitNesseRoot/FitNesse/DotNet/context.txt for
more information.
