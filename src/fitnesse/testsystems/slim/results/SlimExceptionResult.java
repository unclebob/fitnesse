package fitnesse.testsystems.slim.results;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import static fitnesse.slim.SlimServer.*;

public class SlimExceptionResult implements ExceptionResult {
  public static final Pattern EXCEPTION_MESSAGE_PATTERN = Pattern.compile("message:<<(.*?)>>(?!>)", Pattern.DOTALL);
  public static final String DEFAULT_SLIM_EXCEPTION_COMPARATOR = "EXCEPTION:";

  private final String resultKey;
  private final String exceptionValue;
  private TestResult catchException;

  public SlimExceptionResult(String resultKey, String exceptionValue) {
    this.resultKey = resultKey;
    this.exceptionValue = exceptionValue;
    catchException = null;
  }

  @Override
  public ExecutionResult getExecutionResult() {
    if( isIgnoreScriptTestException() || isIgnoreAllTestException()) {
      return ExecutionResult.IGNORE;
    }
    return isStopTestException() ? ExecutionResult.FAIL : ExecutionResult.ERROR;
  }

  public ExecutionResult getIgnoreExecutionResult() {
    if(isIgnoreScriptTestException() || isIgnoreAllTestException()){
      return ExecutionResult.IGNORE;
    }
    return ExecutionResult.ERROR;
  }

  public boolean hasMessage() {
    return getMessage() != null;
  }

  @Override
  public String getMessage() {
    String exceptionMessage = getExceptionMessage();
    if (exceptionMessage != null) {
      return translateExceptionMessage(exceptionMessage);
    }
    return null;
  }

  private String getExceptionMessage() {
    Matcher exceptionMessageMatcher = EXCEPTION_MESSAGE_PATTERN.matcher(exceptionValue);
    if (exceptionMessageMatcher.find()) {
      return exceptionMessageMatcher.group(1);
    } else if( exceptionValue.equals(EXCEPTION_IGNORE_SCRIPT_TEST_TAG)){
      return EXCEPTION_IGNORE_SCRIPT_TEST_TAG;
    } else if (exceptionValue.equals(EXCEPTION_IGNORE_ALL_TESTS_TAG)){
      return EXCEPTION_IGNORE_ALL_TESTS_TAG;
    }
    return null;
  }

  public String getResultKey() {
    return resultKey;
  }

  public String getException() {
    return exceptionValue;
  }

  public boolean isCatchException() {
    return catchException != null;
  }

  public TestResult catchTestResult() {
    return catchException;
  }

  public void setCatchException(TestResult testResult) {
    this.catchException = testResult;
  }

  public boolean isStopTestException() {
    return exceptionValue.contains(EXCEPTION_STOP_TEST_TAG);
  }

  public boolean isStopSuiteException() {
    return exceptionValue.contains(EXCEPTION_STOP_SUITE_TAG);
  }

  public boolean isIgnoreScriptTestException() {
    return exceptionValue.contains(EXCEPTION_IGNORE_SCRIPT_TEST_TAG);
  }

  public boolean isIgnoreAllTestException(){
    return exceptionValue.contains(EXCEPTION_IGNORE_ALL_TESTS_TAG);
  }

  public boolean isNoMethodInClassException() {
    return isExceptionOfType(NO_METHOD_IN_CLASS);
  }

  public boolean isNoInstanceException() {
    return isExceptionOfType(NO_INSTANCE);
  }

  private boolean isExceptionOfType(String type) {
    String exceptionMessage = getExceptionMessage();
    return exceptionMessage != null && exceptionMessage.contains(type);
  }

  private String translateExceptionMessage(String exceptionMessage) {
    String[] tokens = exceptionMessage.split(" ");
    switch (tokens[0]) {
      case COULD_NOT_INVOKE_CONSTRUCTOR:
        return "Could not invoke constructor for " + tokens[1];
      case NO_METHOD_IN_CLASS:
	if (tokens.length == 3){ // Legacy from Slim.Version <= 0.5
          return String.format("Method %s not found in %s", tokens[1], tokens[2]);
	} else {
	  return exceptionMessage.substring(exceptionMessage.indexOf(" ") + 1);
	}
      case NO_CONSTRUCTOR:
        return String.format("Could not find constructor for %s", tokens[1]);
      case NO_CONVERTER_FOR_ARGUMENT_NUMBER:
        return String.format("No converter for %s", tokens[1]);
      case NO_INSTANCE:
        return String.format("The instance %s does not exist", tokens[1]);
      case NO_CLASS:
        return String.format("Could not find class %s", tokens[1]);
      case MALFORMED_INSTRUCTION:
        return String.format("The instruction %s is malformed", exceptionMessage.substring(exceptionMessage.indexOf(" ") + 1));
      case TIMED_OUT:
        return String.format("The instruction timed out after %s seconds", tokens[1]);
      default:
        return exceptionMessage;
    }
  }
}
