This document is intended for those who wish to build FitNesse and do development work.

BUILDING:
The build.xml should be sufficient to build FitNesse as long as junit.jar is in your system classpath.
If not then open build.xml and modify the 'junitJar' property to reflect your environment.

TESTING:
To run the unit tests:
 * build all the class files
 * set your working directory where you unpacked the source
 * add to your classpath:
  * the 'classes' directory
  * junit.jar.
 * run fitnesse.AllTestSuite.

Direct any questions to the FitNesse yahoo group or to micah@objectmentor.com.