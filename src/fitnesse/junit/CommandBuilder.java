package fitnesse.junit;

public class CommandBuilder {
  private static final String COMMON_ARGS = "&nohistory=true&format=java";
  private static final String DEBUG_ARG = "&debug=true";

  private final String pageName;
  private final String pageType;
  private String suiteFilter;
  private String excludeSuiteFilter;
  private boolean debug = true;

  public CommandBuilder(String pageName, String pageType) {
    this.pageName = pageName;
    this.pageType = pageType;

  }

  public CommandBuilder withSuiteFilter(String suiteFilter) {
    this.suiteFilter = suiteFilter;
    return this;
  }

  public CommandBuilder withExcludeSuiteFilter(String excludeSuiteFilter) {
    this.excludeSuiteFilter = excludeSuiteFilter;
    return this;
  }

  public CommandBuilder withDebug(boolean enabled) {
    debug = enabled;
    return this;
  }

  public String build() {
    String command = pageName + "?" + pageType + getCommandArgs();
    if (suiteFilter != null)
      command = command + "&suiteFilter=" + suiteFilter;
    if (excludeSuiteFilter != null)
      command = command + "&excludeSuiteFilter=" + excludeSuiteFilter;
    return command;
  }

  String getCommandArgs() {
    if (debug) {
      return DEBUG_ARG + COMMON_ARGS;
    }
    return COMMON_ARGS;
  }
}