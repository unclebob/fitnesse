package fitnesse.testrunner;

import fitnesse.testsystems.Descriptor;
import fitnesse.wiki.ReadOnlyPageData;

/**
 * Define a (hashable) extract of the test page, to be used as input for building the test system.
 */
public class WikiPageDescriptor implements Descriptor {

  private final ReadOnlyPageData data;
  private final boolean inProcess;
  private final boolean remoteDebug;
  private final String classPath;

  public WikiPageDescriptor(ReadOnlyPageData data, boolean inProcess, boolean remoteDebug, String classPath) {
    this.data = data;
    this.inProcess = inProcess;
    // Debug property should move to ClientBuilder
    this.remoteDebug = remoteDebug;
    this.classPath = classPath;
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
  public String getClassPath() {
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
    return data.getVariable(name);
  }

}
