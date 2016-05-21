# [FitNesse](http://fitnesse.org/)  [![maven central](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse) [![SonarQube Tech Debt](https://img.shields.io/sonar/http/nemo.sonarqube.org/org.fitnesse:fitnesse/tech_debt.svg)](http://nemo.sonarqube.org/dashboard/index?id=org.fitnesse%3Afitnesse)

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

2. Apache Ivy is used for dependency management. Your IDE can be set up to support Ivy. Alternatively,
   ```
   $ ant retrieve
   ```
   will download the dependencies and copy them to `lib/`, from where your
   IDE can pick them up.

#### Import FitNesse in Eclipse

1. Clone the FitNesse Git repository from https://github.com/unclebob/fitnesse.
2. Install the _IvyDE_ plugin if you haven't already done so.
3. Import FitNesse via _Project..._ -> _Java_ -> _Java Project from existing Ant Buildfile_. It asks from a javac target to use. Just pick any. Make sure both `src` and `test` directories are marked as source paths.
4. Open _Properties_ (right-mouse click). In _Java Build Path_ select _Libraries_ and _Add Library..._. Select IvyDE. The _Main_ settings are okay by default. On the _Settings_ tab, select `ivysettings.xml` from the project folder. 
5. While still in the _Properties_ dialog, change the output folders in the _Source_ tab: for the `src` directory, change it to `classes`, for the `test` directory, change it to `test-classes`. Apply the changes are you're good to go.

#### Import FitNesse in IntelliJ IDEA (15)

1. Clone the FitNesse Git repository from https://github.com/unclebob/fitnesse.
2. Install the _IvyIDEA_ plugin if you haven't already done so.
3. From the welcome screen (the one you get when all projects are closed), click _Import Project_.
4. Select the folder containing the fitnesse project.
5. Now the Import project wizard guides you through the import process:
   1. We're not importing from an existing model, so _Create project from existing sources_.
   2. Give it a name.
   3. IntelliJ finds the `src` and `test` folder.
   4. There are no plugins defined, so deselect those. The `test-plugin-*.jar` files are used for some unit tests.
   5. Everything should look fine in the review screen: one module named `fitnesse` with a `src` and a `test` folder.
   6. Select a JDK. At least Java 7 is required.
   7. Now IntelliJ starts looking for frameworks. It should come up with IvyIDEA.
   8. Finish the wizard. The project should be opened.
6. We're almost there. The IvyIDEA plugin is not completely configured yet. To fix this open _File_ -> _Project Structure..._ go to _Modules_:
   1. Select the `fitnesse` module and set the output path to the `classes` folder and the test output path to `test-classes`. This ensures IntelliJ works nicely with the Ant tasks we want to execute as part of the build process.
   2. Select IvyIDEA. Tell it to use _Module specific ivy settings_ and select `ivysettings.xml` from the project folder.
7. Open the Ant Build tool, add the projects `build.xml` file and right click on the `post-compile` task. Select _Execute on_ -> _Before Compilation_. Apply the changes are you're good to go.
