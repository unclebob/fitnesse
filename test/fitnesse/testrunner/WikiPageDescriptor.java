package fitnesse.testrunner;

import java.io.File;
import java.util.Arrays;

import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.CompositeExecutionLogListener;
import fitnesse.testsystems.Descriptor;
import fitnesse.wiki.WikiPage;

/**
 * Define a (hashable) extract of the test page, to be used as input for building the test system.
 */
public class WikiPageDescriptor implements Descriptor {

  private final WikiPage page;
  private final boolean inProcess;
  private final boolean remoteDebug;
  private final ClassPath classPath;
  private final CompositeExecutionLogListener executionLogListener;

  public WikiPageDescriptor(WikiPage data, boolean inProcess, boolean remoteDebug, String... classPath) {
    this.page = data;
    this.inProcess = inProcess;
    // Debug property should move to ClientBuilder
    this.remoteDebug = remoteDebug;
    this.classPath = new ClassPath(Arrays.asList(classPath), File.pathSeparator);
    this.executionLogListener = new CompositeExecutionLogListener();
  }

  @Override
  public String getTestSystem() {
    String testSystemName = getVariable(WikiPageIdentity.TEST_SYSTEM);
    if (testSystemName == null)
      return "fit";
    return testSystemName;
  }

  @Override
  public String getTestSystemType() {
    return getTestSystem().split(":")[0];
  }

  @Override
  public ClassPath getClassPath() {
    return classPath;
  }

  @Override
  public boolean runInProcess() {
    return inProcess;
  }

  @Override
  public boolean isDebug() {
    return remoteDebug;
  }

  // Generic entry point for everything the test system needs to know.
  @Override
  public String getVariable(String name) {
    return page.getVariable(name);
  }

  @Override
  public CompositeExecutionLogListener getExecutionLogListener() {
    return executionLogListener;
  }

}
