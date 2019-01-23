// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain.ant;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Task to run fit tests. This task runs fitnesse tests and publishes the results.
 * <pre>
 * Usage:
 * &lt;taskdef name=&quot;execute-fitnesse-tests&quot;
 *     classname=&quot;fitnesse.ant.ExecuteFitnesseTestsTask&quot;
 *     classpathref=&quot;classpath&quot; /&gt;
 * OR
 * &lt;taskdef classpathref=&quot;classpath&quot;
 *             resource=&quot;tasks.properties&quot; /&gt;
 *
 * &lt;execute-fitnesse-tests
 *     suitepage=&quot;FitNesse.SuiteAcceptanceTests&quot;
 *     fitnesseport=&quot;8082&quot;
 *     resultsdir=&quot;${results.dir}&quot;
 *     resultshtmlpage=&quot;fit-results.html&quot;
 *     classpathref=&quot;classpath&quot; /&gt;
 * </pre>
 */
public class ExecuteFitnesseTestsTask extends Task {
  private String fitnesseHost = "localhost";
  private int fitnessePort;
  private String suitePage;
  private String suiteFilter;
  private String resultsDir = ".";
  private String resultsHTMLPage;
  private String resultsXMLPage;
  private boolean debug = true;
  private boolean verbose = true;
  private boolean failOnError = true;
  private String testRunnerClass = "fitnesse.runner.TestRunner";
  private Path classpath;
  private String resultProperty;

  @Override
  public void execute() throws BuildException {
    try {
      int exitCode = executeRunnerClassAsForked();
      if (exitCode != 0) {
        log("Finished executing FitNesse tests: " + exitCode + " failures/exceptions");
        if (failOnError) {
          throw new BuildException(exitCode + " FitNesse test failures/exceptions");
        } else {
          getProject().setNewProperty(resultProperty, String.valueOf(exitCode));
        }
      } else {
        log("Fitnesse Tests executed successfully");
      }
    }
    catch (Exception e) {
      if (failOnError) {
        throw new BuildException(
            "Got an unexpected error trying to run the fitnesse tests : " + e.getMessage(), e);
      } else {
        e.printStackTrace();
      }
    }
  }

  private int executeRunnerClassAsForked() throws BuildException {
    CommandlineJava cmd = initializeJavaCommand();

    Execute execute = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
    String[] commandLine = cmd.getCommandline();
    log("Executing: " + StringUtils.join(Arrays.asList(commandLine), " "));
    execute.setCommandline(commandLine);
    execute.setNewenvironment(false);
    execute.setAntRun(getProject());

    log(cmd.describeCommand(), Project.MSG_VERBOSE);
    int retVal;
    try {
      retVal = execute.execute();
    }
    catch (IOException e) {
      throw new BuildException("Process fork failed.", e, getLocation());
    }

    return retVal;
  }

  private CommandlineJava initializeJavaCommand() {
    CommandlineJava cmd = new CommandlineJava();
    cmd.setClassname(testRunnerClass);
    appendDebugArgument(cmd);
    appendVerboseArgument(cmd);
    appendHtmlResultPage(cmd);
    appendXmlResultPage(cmd);
    appendSuiteFilter(cmd);
    cmd.createArgument().setValue(fitnesseHost);
    cmd.createArgument().setValue(String.valueOf(fitnessePort));
    cmd.createArgument().setValue(suitePage);
    cmd.createClasspath(getProject()).createPath().append(classpath);
    return cmd;
  }

  private void appendDebugArgument(CommandlineJava cmd) {
    if (debug)
      cmd.createArgument().setValue("-debug");
  }

  private void appendVerboseArgument(CommandlineJava cmd) {
    if (verbose)
      cmd.createArgument().setValue("-v");
  }

  private void appendHtmlResultPage(CommandlineJava cmd) {
    if (resultsHTMLPage != null) {
      String resultsHTMLPagePath = new File(resultsDir, resultsHTMLPage).getAbsolutePath();
      cmd.createArgument().setValue("-html");
      cmd.createArgument().setValue(resultsHTMLPagePath);
    }
  }

  private void appendXmlResultPage(CommandlineJava cmd) {
    if (resultsXMLPage != null) {
      String resultsHTMLPagePath = new File(resultsDir, resultsXMLPage).getAbsolutePath();
      cmd.createArgument().setValue("-xml");
      cmd.createArgument().setValue(resultsHTMLPagePath);
    }
  }

  private void appendSuiteFilter(CommandlineJava cmd) {
    if (suiteFilter != null) {
      cmd.createArgument().setValue("-suiteFilter");
      cmd.createArgument().setValue(suiteFilter);
    }
  }

  /**
   * Set host address on which Fitnesse is running. Defaults to 'localhost'.
   *
   * @param fitnesseHost host address on which Fitnesse is running
   */
  public void setFitnesseHost(String fitnesseHost) {
    this.fitnesseHost = fitnesseHost;
  }

  /**
   * Set Classpath of the TestRunner class. <b>MUST SET</b>
   *
   * @param classpath Classpath of the TestRunner class
   */
  public void setClasspath(Path classpath) {
    this.classpath = classpath;
  }

  /**
   * Set name of the filter to be passed to TestRunner to specify a subset of tests to run.
   *
   * @param suiteFilter name of the filter to be passed to TestRunner for filtering
   */
  public void setSuiteFilter(String suiteFilter) {
    this.suiteFilter = suiteFilter;
  }

  /**
   * Set debug mode. Defaults to 'true'.
   *
   * @param debug enable or disable debug mode
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Enable or disable to fail the build if any Fitnesse tests fail. Defaults to 'true'.
   *
   * @param failOnError Enable or disable to fail the build if any Fitnesse tests fail
   */
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  /**
   * Set port on which fitnesse would run. <b>MUST SET.</b>.
   *
   * @param fitnessePort port on which fitnesse would run
   */
  public void setFitnessePort(int fitnessePort) {
    this.fitnessePort = fitnessePort;
  }

  /**
   * Set name of the property which will store the test results. Only valid if failOnError attribute is set to false.
   *
   * @param resultProperty name of the property which will store the test results
   */
  public void setResultProperty(String resultProperty) {
    this.resultProperty = resultProperty;
  }

  /**
   * Set the path to the folder that will contain the fitnesse results page after execution. Only valid if resultsHTMLPage or
   * resultsXMLPage attributes are set. Defaults to current directory.
   *
   * @param resultsDir path to the folder that will contain the fitnesse results page after execution.
   */
  public void setResultsDir(String resultsDir) {
    this.resultsDir = resultsDir;
  }

  /**
   * Set the filename for storing the results in HTML format
   *
   * If set, stores the fitnesse results in HTML format under the resultsdir folder with the given name. The file name
   * must have a '.html' extension.
   *
   * @param resultsHTMLPage set the filename for storing the results in HTML format
   */
  public void setResultsHTMLPage(String resultsHTMLPage) {
    this.resultsHTMLPage = resultsHTMLPage;
  }

  /**
   * Set the filename for storing the results in XML format
   *
   * If set, stores the fitnesse results in XML format under the resultsdir folder with the given name. The file name
   * must have a '.xml' extension.
   *
   * @param resultsXMLPage set the filename for storing the results in XML format
   */
  public void setResultsXMLPage(String resultsXMLPage) {
    this.resultsXMLPage = resultsXMLPage;
  }

  /**
   * Set the fully qualifies class name of the fitnesse testrunner class. Defaults to 'fitnesse.runner.TestRunner'.
   *
   * @param runnerClass Fully qualifies class name of the fitnesse testrunner class
   */
  public void setTestRunnerClass(String runnerClass) {
    testRunnerClass = runnerClass;
  }

  /**
   * Set the partial URL of the wiki page which is declared as a Suite. Ex: FrontPage.SmokeTest,
   * FitNesse.SuiteAcceptanceTests, or FitNesse.AcceptanceTestsSuite. <b>MUST SET.</b>
   *
   * @param suitePage  partial URL of the wiki page which is declared as a Suite
   */
  public void setSuitePage(String suitePage) {
    this.suitePage = suitePage;
  }

  /**
   * Set verbose mode. Defaults to 'true'.
   *
   * @param verbose verbose mode
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public Path createClasspath() {
    if (classpath == null) {
      classpath = new Path(getProject());
    }
    return classpath.createPath();
  }

  public void setClasspathRef(Reference r) {
    createClasspath().setRefid(r);
  }
}
