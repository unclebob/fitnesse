// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesseMain.FitNesseMain;

/**
 * Task to run fit tests. This task starts the fit server, runs fitnesse tests and publishes the results. <p/>
 * <p/>
 * <pre>
 * Usage:
 * &lt;taskdef name=&quot;run-fitnesse-tests&quot; classname=&quot;fitnesse.ant.TestRunnerTask&quot; classpathref=&quot;classpath&quot; /&gt;
 * OR
 * &lt;taskdef classpathref=&quot;classpath&quot; resource=&quot;tasks.properties&quot; /&gt;
 * &lt;p/&gt;
 * &lt;run-fitnesse-tests wikidirectoryrootpath=&quot;.&quot; suitepage=&quot;FitNesse.SuiteAcceptanceTests&quot; fitnesseport=&quot;8082&quot; resultsdir=&quot;${results.dir}&quot; resultshtmlpage=&quot;fit-results.html&quot; resultsxmlpage=&quot;fit-results.xml&quot; classpathref=&quot;classpath&quot; /&gt;
 * </pre>
 */
public class TestRunnerTask extends Task {
  private String wikiDirectoryRootPath;
  private int fitnessePort = 8082;
  private String suitePage;
  private String resultsDir = ".";
  private String resultsXMLPage;
  private boolean verbose = true;
  private boolean failOnError = true;
  private String testRunnerClass = "fitnesse.runner.TestRunner";
  private Path classpath;
  private String resultProperty;

  @Override
  public void execute() throws BuildException {
    startFitNesse();
    try {
      executeTests();
    } catch (Exception e) {
      if (failOnError)
        throw new BuildException("Got an unexpected error trying to run the fitnesse tests : " + e.getMessage(), e);
      else
        e.printStackTrace();
    } finally {
      stopFitNesse();
    }
  }

  private void executeTests() {
    int exitCode = executeRunnerClassAsForked();
    if (exitCode != 0) {
      log("Finished executing FitNesse tests: " + exitCode + " failures/exceptions");
      if (failOnError)
        throw new BuildException(exitCode + " FitNesse test failures/exceptions");
      else
        getProject().setNewProperty(resultProperty, String.valueOf(exitCode));
    } else
      log("Fitnesse Tests executed successfully");
  }

  private void stopFitNesse() {
    FitNesseContext context = new FitNesseContext();
    context.port = fitnessePort;
    try {
      new FitNesse(context).stop();
    } catch (Exception e) {
      throw new BuildException("Failed to stop FitNesse. Error Msg: " + e.getMessage(), e);
    }
  }

  private void startFitNesse() {
    try {
      FitNesseMain.main(new String[]{"-p", String.valueOf(fitnessePort), "-d", wikiDirectoryRootPath, "-e", "0", "-o"});
    } catch (Exception e) {
      throw new BuildException("Failed to start FitNesse. Error Msg: " + e.getMessage(), e);
    }
    log("Sucessfully Started Fitnesse on port " + fitnessePort);
  }

  private int executeRunnerClassAsForked() throws BuildException {
    CommandlineJava cmd = initializeJavaCommand();

    Execute execute = new Execute(new LogStreamHandler(this, Project.MSG_INFO, Project.MSG_WARN));
    execute.setCommandline(cmd.getCommandline());
    execute.setNewenvironment(false);
    execute.setAntRun(getProject());

    log(cmd.describeCommand(), Project.MSG_VERBOSE);
    int retVal;
    try {
      retVal = execute.execute();
    } catch (IOException e) {
      throw new BuildException("Process fork failed.", e, getLocation());
    }

    return retVal;
  }

  private CommandlineJava initializeJavaCommand() {
    CommandlineJava cmd = new CommandlineJava();
    cmd.setClassname(testRunnerClass);
    cmd.createVmArgument().setValue("-Xmx100M");
    if (verbose)
      cmd.createArgument().setValue("-v");
    if (resultsXMLPage != null) {
      String resultsHTMLPagePath = new File(resultsDir, resultsXMLPage).getAbsolutePath();
      cmd.createArgument().setValue("-xml");
      cmd.createArgument().setValue(resultsHTMLPagePath);
    }
    cmd.createArgument().setValue("localhost");
    cmd.createArgument().setValue(String.valueOf(fitnessePort));
    cmd.createArgument().setValue(suitePage);
    cmd.createClasspath(getProject()).createPath().append(classpath);
    return cmd;
  }

  /**
   * Classpath of the TestRunner class. <b>MUST SET</b>
   *
   * @param classpath
   */
  public void setClasspath(Path classpath) {
    this.classpath = classpath;
  }


  /**
   * Will fail the build if any Fitnesse tests fail. Defaults to 'true'.
   *
   * @param failOnError
   */
  public void setFailOnError(boolean failOnError) {
    this.failOnError = failOnError;
  }

  /**
   * Port on which fitnesse would run. Defaults to <b>8082</b>.
   *
   * @param fitnessePort
   */
  public void setFitnessePort(int fitnessePort) {
    this.fitnessePort = fitnessePort;
  }

  /**
   * Name of the property which will store the test results. Only valid if failOnError attribute is set to false.
   *
   * @param resultProperty
   */
  public void setResultProperty(String resultProperty) {
    this.resultProperty = resultProperty;
  }

  /**
   * Path to the folder that will contain the fitnesse results page after execution. Only valid if resultsHTMLPage or
   * resultsXMLPage attributes are set. Defaults to current directory.
   *
   * @param resultsDir
   */
  public void setResultsDir(String resultsDir) {
    this.resultsDir = resultsDir;
  }

  /**
   * If set, stores the fitnesse results in XML format under the resultsdir folder with the given name. The file name
   * must have a '.xml' extension.
   *
   * @param resultsXMLPage
   */
  public void setResultsXMLPage(String resultsXMLPage) {
    this.resultsXMLPage = resultsXMLPage;
  }

  /**
   * Fully qualifies class name of the fitnesse testrunner class. Defaults to 'fitnesse.runner.TestRunner'.
   *
   * @param runnerClass
   */
  public void setTestRunnerClass(String runnerClass) {
    this.testRunnerClass = runnerClass;
  }

  /**
   * Partial URL of the wiki page which is declared as a Suite. Ex: FrontPage.SmokeTest,
   * FitNesse.SuiteAcceptanceTests, or FitNesse.AcceptanceTestsSuite. <b>MUST SET.</b>
   *
   * @param suitePage
   */
  public void setSuitePage(String suitePage) {
    this.suitePage = suitePage;
  }

  /**
   * Set verbose mode. Defaults to 'true'.
   *
   * @param verbose
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Path to the FitnesseRoot filder which contains all the wiki pages. <b>MUST SET</b>.
   *
   * @param wikiDirectoryRootPath
   */
  public void setWikiDirectoryRootPath(String wikiDirectoryRootPath) {
    this.wikiDirectoryRootPath = wikiDirectoryRootPath;
  }

  public Path createClasspath() {
    if (classpath == null)
      classpath = new Path(getProject());
    return classpath.createPath();
  }

  public void setClasspathRef(Reference r) {
    createClasspath().setRefid(r);
  }
}
