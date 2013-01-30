package fitnesse.testsystems.slim.results;

import fitnesse.testsystems.ExecutionResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fitnesse.slim.SlimServer.*;

public class ExceptionResult extends TestResult {
  public static final Pattern EXCEPTION_MESSAGE_PATTERN = Pattern.compile("message:<<(.*)>>");

  private final String resultKey;
  private final String exceptionValue;

  public ExceptionResult(String resultKey, String exceptionValue) {
    super(null, null, null, exceptionValue.contains(EXCEPTION_STOP_TEST_TAG) ? ExecutionResult.FAIL : ExecutionResult.ERROR);
    this.resultKey = resultKey;
    this.exceptionValue = exceptionValue;
  }

  @Override
  public boolean hasMessage() {
    return getMessage() != null;
  }

  @Override
  public String getMessage() {
    Matcher exceptionMessageMatcher = EXCEPTION_MESSAGE_PATTERN.matcher(exceptionValue);
    if (exceptionMessageMatcher.find()) {
      String exceptionMessage = exceptionMessageMatcher.group(1);
      return translateExceptionMessage(exceptionMessage);
    }
    return null;
  }

  public String getResultKey() {
    return resultKey;
  }

  //  private String toHtml() {
//    return String.format("<span class=\"%s\">%s</span>", htmlClass(), processException());
//  }
//
//  private String processException() {
//    Matcher exceptionMessageMatcher = EXCEPTION_MESSAGE_PATTERN.matcher(exceptionValue);
//    if (exceptionMessageMatcher.find()) {
//      String exceptionMessage = exceptionMessageMatcher.group(1);
//      return translateExceptionMessage(exceptionMessage);
//    } else {
//      return exceptionResult(resultKey);
//    }
//  }

  private String translateExceptionMessage(String exceptionMessage) {
    String tokens[] = exceptionMessage.split(" ");
    if (tokens[0].equals(COULD_NOT_INVOKE_CONSTRUCTOR))
      return "Could not invoke constructor for " + tokens[1];
    else if (tokens[0].equals(NO_METHOD_IN_CLASS))
      return String.format("Method %s not found in %s", tokens[1], tokens[2]);
    else if (tokens[0].equals(NO_CONSTRUCTOR))
      return String.format("Could not find constructor for %s", tokens[1]);
    else if (tokens[0].equals(NO_CONVERTER_FOR_ARGUMENT_NUMBER))
      return String.format("No converter for %s", tokens[1]);
    else if (tokens[0].equals(NO_INSTANCE))
      return String.format("The instance %s does not exist", tokens[1]);
    else if (tokens[0].equals(NO_CLASS))
      return String.format("Could not find class %s", tokens[1]);
    else if (tokens[0].equals(MALFORMED_INSTRUCTION))
      return String.format("The instruction %s is malformed", exceptionMessage.substring(exceptionMessage.indexOf(" ") + 1));

    return exceptionMessage;
  }

//  private String exceptionResult(String resultKey) {
//    return String.format("Exception: <a href=#%s>%s</a>", resultKey, resultKey);
//  }

//  public String toString() {
//    return String.format("%s(%s)", htmlClass(), processException());
//  }
}
