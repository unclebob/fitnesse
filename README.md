# [FitNesse](https://fitnesse.org/)  [![maven central](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse) [![SonarQube Tech Debt](https://img.shields.io/sonar/http/nemo.sonarqube.org/org.fitnesse:fitnesse/tech_debt.svg)](http://nemo.sonarqube.org/dashboard/index?id=org.fitnesse%3Afitnesse)

Welcome to FitNesse, the fully integrated stand-alone acceptance testing framework and wiki.

To get started, check out the [FitNesse website](https://fitnesse.org/)!



## Quick start

* [A One-Minute Description of FitNesse](https://fitnesse.org/FitNesse/UserGuide/OneMinuteDescription.html)
* [Download FitNesse](https://fitnesse.org/FitNesseDownload.html) and [Plugins](https://fitnesse.org/PlugIns.html)
* [The FitNesse User Guide](https://fitnesse.org/FitNesse/UserGuide.html)



## Bug tracker

Have a bug or a feature request? [Please open a new issue](https://github.com/unclebob/fitnesse/issues).


## Edge builds

The latest stable build of FitNesse can be [downloaded here](https://github.com/unclebob/fitnesse/actions) by clicking the latest workflow and clicking the `libs` artifact.

**Note**: the `libs` artifact contains 2 jars. `fitnesse.jar` is for use in Maven or Ivy. Users who just want to run FitNesse by itself should use `fitnesse-standalone.jar` instead of `fitnesse.jar`.

## Developers

Issues and pull requests are administered at [GitHub](https://github.com/unclebob/fitnesse/issues).

### Building

A proper internet connection is sufficient to build FitNesse. The build process will bootstrap itself by downloading [Gradle](http://gradle.org) and from there will download the dependencies required to build and test FitNesse.

To build and run all tests, run the command

```
$ ./gradlew
```

which builds the `all` target.

NB. On windows call `gradlew.bat` instead of `./gradlew`.

### Running

To start the FitNesse wiki locally, for example to browse the local version of the User Guide

```
$ ./gradlew run
```

### Testing

To run the unit tests:

```
$ ./gradlew test
```

To run the acceptance tests:

```
$ ./gradlew acceptanceTest
```



### Working with Eclipse and IntelliJ

There are a few things to keep in mind when working from an IDE:

1. The Gradle build  does some extra things apart from compiling the code.
    * It sets the FitNesse version in a META-INF/FitNesseVersion.txt
    * It copies the dependencies to the lib folder so they can be used by the acceptance tests.

   Perform a
   ```
   $ ./gradlew copyRuntimeLibs
   ```
   to execute the copy action. In your IDE it is possible to define "post-compilation" steps. If
   you set the "post-compile" target from the build file, you won't have any trouble with
   cleaning, building and executing tests from your IDE.


#### Import FitNesse in Eclipse

1. Clone the FitNesse Git repository from https://github.com/unclebob/fitnesse.
2. Import FitNesse via _File_ -> _Import..._ -> _Gradle Project_.
3. Select the just cloned project folder. Follow the wizard.
4. Ensure the project properties have a Java 11 compiler or newer set.

#### Import FitNesse in IntelliJ IDEA (2025.1)

1. Create a new project from https://github.com/unclebob/fitnesse (i.e. from the menu select _File_ -> _New_ -> _Project from Version Control..._,
   ensure the 'Version control' selectbox contains 'Git', enter the project's URL and press 'Clone').
2. Ensure the project structure is set to use Java 11 SDK or newer.
3. Open the Gradle pane Tool Window (little elephant icon on the right side of the screen). Its 'Tasks' folder contains
   the various gradle tasks defined for the project.
4. To launch the wiki: open 'Tasks/other' and look for the cog-icon 'run', right-click and select either 'Run' or 'Debug'
5. Wiki should now be accessible by opening http://localhost:8001 in your browser.





### Working with VSCode and Docker Dev Container

For this option, nothing other than VSCode and Docker needs to be installed.

1. Clone the FitNesse Git repository from https://github.com/unclebob/fitnesse.
2. Open the fitnesse folder in VSCode
3. Wait for the popup `Folder contains a Dev Container configuration file. Reopen folder to develop in a container (learn more).`
4. Click the button `Reopen in Container`
5. Wait until init routines are finished
6. Use _Run & Debug_ or _Gradle_ buttons in the left toolbar



### The release process

FitNesse releases are created using https://github.com/fitnesse/fitnesse-release
