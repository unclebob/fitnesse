package fitnesse.testrunner;

import fitnesse.wiki.ReadOnlyPageData;

public class WikiPageIdentity {

  public static final String COMMAND_PATTERN = "COMMAND_PATTERN";
  public static final String TEST_RUNNER = "TEST_RUNNER";
  public static final String TEST_SYSTEM = "TEST_SYSTEM";
  private ReadOnlyPageData data;

  public WikiPageIdentity(ReadOnlyPageData data) {
    this.data = data;
  }

  public String getVariable(String name) {
    return data.getVariable(name);
  }

  public String testSystem() {
    String testSystemName = getVariable(TEST_SYSTEM);
    if (testSystemName == null)
      return "fit";
    return testSystemName;
  }

  private String testRunner() {
    String program = getVariable(TEST_RUNNER);
    if (program == null)
      program = "";
    return program;
  }

  private String commandPattern() {
    String testRunner = getVariable(COMMAND_PATTERN);
    if (testRunner == null)
      testRunner = "";
    return testRunner;
  }

  @Override
  public int hashCode() {
    return testSystem().hashCode() ^ testRunner().hashCode() ^ commandPattern().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;

    WikiPageIdentity identity = (WikiPageIdentity) obj;
    return identity.testSystem().equals(testSystem()) &&
            identity.testRunner().equals(testRunner()) &&
            identity.commandPattern().equals(commandPattern());
  }
}
