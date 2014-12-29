# [FitNesse](http://fitnesse.org/)  [![maven central](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse)

Welcome to FitNesse, the fully integrated stand-alone acceptance testing framework and wiki.

To get started, check out [http://fitnesse.org](http://fitnesse.org)!



## Quick start

* [A One-Minute Description of FitNesse](http://fitnesse.org/FitNesse.UserGuide.OneMinuteDescription)
* [Download FitNesse](http://fitnesse.org/FitNesseDownLoad) and [Plugins](http://fitnesse.org/PlugIns)
* [The FitNesse User Guide](http://fitnesse.org/.FitNesse.UserGuide)



## Bug tracker

Have a bug or a feature request? [Please open a new issue](https://github.com/unclebob/fitnesse/issues). 


## Community

Have a question that's not a feature request or bug report? [Ask on the mailing list.](http://groups.yahoo.com/group/fitnesse)

## Edge builds

The latest stable build of FitNesse can be [downloaded here](https://cleancoder.ci.cloudbees.com/job/fitnesse/lastStableBuild/).

**Note**: the edge Jenkins build produces 2 jars. `fitnesse.jar` is for use in Maven or Ivy. Users who just want to run FitNesse by itself should download `fitnesse-standalone.jar` instead of `fitnesse.jar`.

## Developers

Issues and pull requests are administered at [GitHub](https://github.com/unclebob/fitnesse/issues).

### Building

[Apache Ant](http://ant.apache.org/) and a proper internet connection is sufficient to build FitNesse. The build process will bootstrap itself by downloading Ivy (dependency management) and from there will download the modules required to build and test FitNesse.

To build and run all tests, run the command

```
$ ant
``` 

which builds the `all` target. 

### Running

To start the FitNesse wiki locally, for example to browse the local version of the User Guide

```
$ ant run
```

### Testing

To run the unit tests:

```
$ ant unit_test
```

To run the acceptance tests:

```
$ ant acceptance_tests
```

There is a second source directory, `srcFitServerTests`, which contains units
tests that test invocation of Fit servers written in Ruby, C++, and .NET. These
tests are not run as part of the normal ant test-related targets. When using an
IDE, make sure it does not invoke these tests when running the "normal" tests
under the `src` directory.

Direct any questions to the [FitNesse Yahoo group](https://groups.yahoo.com/neo/groups/fitnesse/info).


### Working with Eclipse and IntelliJ

There are a few things to keep in mind when working from an IDE:

1. The Ant build file does some extra things apart from compiling the code.
    * It sets the FitNesse version in a META-INF/FitNesseVersion.txt
    * It copies the dependencies to the lib folder so they can be used by the acceptance tests.

   Perform a
   ```
   $ ant post-compile
   ```
   to execute those actions. In your IDE it is possible to define "post-compilation" steps. If
   you set the "post-compile" target from the build file, you won't have any trouble with
   cleaning, building and executing tests from your IDE.

2. Apache Ivy is used for dependency management. Your IDE can be set up to support Ivy.
    * In IntelliJ set IvyIDEA in "Project Structure" -> "Modules" -> "Dependencies".
    * In Eclipse, install IvyDE and set it up.

   Alternatively,
   ```
   $ ant retrieve
   ```
   will download the dependencies and copy them to lib/, from where your
   IDE can pick them up.

