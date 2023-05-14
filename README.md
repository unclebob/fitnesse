# [FitNesse](http://fitnesse.org/)  [![maven central](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.fitnesse/fitnesse) [![SonarQube Tech Debt](https://img.shields.io/sonar/http/nemo.sonarqube.org/org.fitnesse:fitnesse/tech_debt.svg)](http://nemo.sonarqube.org/dashboard/index?id=org.fitnesse%3Afitnesse)

Welcome to FitNesse, the fully integrated stand-alone acceptance testing framework and wiki.

To get started, check out the [FitNesse website](https://fitnesse.github.io/fitnessedotorg/)!



## Quick start

* [A One-Minute Description of FitNesse](https://fitnesse.github.io/fitnessedotorg/FitNesse/UserGuide/OneMinuteDescription.html)
* [Download FitNesse](https://fitnesse.github.io/fitnessedotorg/FitNesseDownload.html) and [Plugins](https://fitnesse.github.io/fitnessedotorg/PlugIns.html)
* [The FitNesse User Guide](https://fitnesse.github.io/fitnessedotorg/FitNesse/UserGuide.html)



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
4. Ensure the project properties have a Java 8 compiler or newer set.

#### Import FitNesse in IntelliJ IDEA (16)

1. Clone the FitNesse Git repository from https://github.com/unclebob/fitnesse.
2. From the welcome screen (the one you get when all projects are closed), click _Import Project_.
3. Select the file `build.gradle` in the fitnesse folder.
4. Follow the wizard. Deselect the option `Create separate module per source set`. You can use the
   `Use gradle wrapper task configuration`. Use Java 8 or newer. It should find source and test folders and
    show you two modules: `fitnesse` and `:buildSrc`; import both.
5. Open the Gradle Build tool, select the task `copyRuntimeLibs` and (right-click) mark it as _Execute After Make_.

### The release process

FitNesse releases are deployed to Maven Central via [OSS Sonatype](https://oss.sonatype.org/#welcome).

Sufficient permissions are required to perform a release.

Release builds denote "blessed" releases. Those are tagged in Git along with being released. The releases will be available from
Maven Central.

   ```
   $ ./gradlew release
   ```

For this to work you'll need to add some properties to your ~/.gradle/gradle.properties

```
sonatypeUsername=...
sonatypePassword=...

signing.keyId=...
signing.password=...
signing.secretKeyRingFile=...
```

Details on what values to provide can be found on
[Deploy to Maven Central using API key ](https://blog.solidsoft.pl/2015/09/08/deploy-to-maven-central-using-api-key-aka-auth-token/)
and the [documentation of the Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html).
