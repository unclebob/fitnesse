# [FitNesse](http://fitnesse.org/)

Welcome to FitNesse, the fully integrated stand-alone acceptance testing framework and wiki.

To get started, check out [http://fitnesse.org](http://fitnesse.org)!



## Quick start

* [A One-Minute Description of FitNesse](http://fitnesse.org/FitNesse.UserGuide.OneMinuteDescription)
* [Download FitNesse and Plugins](http://fitnesse.org/FrontPage.FitNesseDevelopment.DownLoad)
* [The FitNesse User Guide](http://fitnesse.org/.FitNesse.UserGuide)



## Bug tracker

Have a bug or a feature request? [Please open a new issue](https://github.com/unclebob/fitnesse/issues). 


## Community

Have a question that's not a feature request or bug report? [Ask on the mailing list.](http://groups.yahoo.com/group/fitnesse)

## Edge builds

The latest stable build of FitNesse can be [downloaded here](https://cleancoder.ci.cloudbees.com/job/fitnesse/lastStableBuild/).

**Note**: the edge Jenkins build produces 2 jars. `fitnesse.jar` is for use in Maven or Ivy. Users who just want to run FitNesse by itself should download `fitnesse-standalone.jar` instead of `fitnesse.jar`.

## Developers

Check out the [FitNesse Story Backlog and Issue Tracking](https://www.pivotaltracker.com/projects/44141) on Pivotal Tracker.

### Building

The `build.xml` and a proper internet connection is sufficient to build FitNesse.
The build process will bootstrap itself by downloading Ivy (dependency management) and from there will download the modules required to build and test FitNesse.

To build and run all tests, run the command

```
$ ant
``` 

which builds the `all` target. 


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

Direct any questions to the FitNesse yahoo group or to [unclebob](https://www.github.com/unclebob).


### Working with Eclipse and IntelliJ

There are a few things to keep in mind when working from an IDE:

1. The ant build generates two files from the templates in the "templates"
   directory:
   * `FrontPage.content.txt.template` is used to generate
     `FitNesseRoot/FrontPage/context.txt`;
   * `FitNesseVersion.java.template` is used to generate
     `src/fitnesse/FitNesseVersion.java`.

   You can execute

   ```
   $ ant all
   ```

   to generate those files.

2. Apache Ivy is used for dependency management. You can either install an Ivy
   plugin in your preferred IDE or run
   ```
   $ ant retrieve
   ```
   that will download the dependencies and copy them to lib/, from where your
   IDE can pick them up.


### .NET Support (8/6/2008)

We re-installed the dotnet/*.dll and dotnet/*.exe files, taking them from the
`fitnessedotnet` release on Sourceforge. This will allow the .NET Acceptance
Tests to run right out of this distribution. However, you should consider using
[FitSharp](http://www.syterra.com/FitSharp.html). See the page `FitNesseRoot/FitNesse/DotNet/context.txt` for
more information.
