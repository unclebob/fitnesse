This document is intended for those who wish to build FitNesse and do development work.

BUILDING:
The build.xml should be sufficient to build FitNesse as long as junit.jar is in your system classpath.
If not then open build.xml and modify the 'junitJar' property to reflect your environment.

To build and run all tests, run the command
 * ant
 
which builds the '''all''' target. 

TESTING:
To run the unit tests:
 * build all the class files
 * set your working directory where you unpacked the source
 * add to your classpath:
  * the 'classes' directory
  * junit.jar.
 * run all tests.

There is a second source directory, "srcFitServerTests", which contains units tests that test 
invocation of Fit servers written in Ruby, C++, and .NET. These tests are not run as part of the normal
ant test-related targets. When using an IDE, make sure it does not invoke these tests when running the 
"normal" tests under the "src" directory.

Direct any questions to the FitNesse yahoo group or to unclebob@cleancoder.com.

Note: Working with Eclipse and IntelliJ

The ant build generates two files from the templates in the "templates" directory:
 * FrontPage.content.txt.template is used to generate FitNesseRoot/FrontPage/context.txt
 * FitNesseVersion.java.template is used to generate src/fitnesse/FitNesseVersion.java
 
So, run "ant all" before loading a fresh checkout into an IDE.

Note: .NET Support (8/6/2008)

We re-installed the dotnet/*.dll and dotnet/*.exe files, taking them from the "fitnessedotnet" release on Sourceforge. This will allow the .NET Acceptance Tests to run right out of this distribution. However, you should consider using "fitnessedotnet". See the page FitNesseRoot/FitNesse/DotNet/context.txt for more information.