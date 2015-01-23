package fitnesse.testsystems;


public enum ExecutionResult {
  ERROR,
  FAIL,
  IGNORE,
  PASS;

  @Override
  public String toString() {
	  return this.name().toLowerCase();
  }
	  
  public static ExecutionResult getExecutionResult(String relativeName, TestSummary testSummary) {
	  return getExecutionResult(relativeName, testSummary, false);
  }
  
  public static ExecutionResult getExecutionResult(String relativeName, TestSummary testSummary, boolean wasInterrupted) {
    if (testSummary.getWrong() > 0 || wasInterrupted) {
      return FAIL;
    } else if (testSummary.getExceptions() > 0) {
      return ERROR;
    } else if (((isSuiteMetaPage(relativeName) && testSummary.getIgnores() > 0)
    		|| (!isSuiteMetaPage(relativeName) && testSummary.getIgnores() >= 0)) && testSummary.getRight() == 0) {
      return IGNORE;
	  }
    return PASS;
  }
  
  public static ExecutionResult getExecutionResult(TestSummary testSummary) {
    if (testSummary.getWrong() > 0) {
      return FAIL;
    } else if (testSummary.getExceptions() > 0) {
      return ERROR;
    } else if (testSummary.getRight() > 0) {
      return PASS;
    }
    return IGNORE;
  }
  
  public static boolean isSuiteMetaPage(String relativeName) {
    return relativeName != null && (relativeName.equals("SuiteSetUp")
        || relativeName.endsWith(".SuiteSetUp")
        || relativeName.equals("SuiteTearDown")
        || relativeName.endsWith(".SuiteTearDown"));
  }

}
