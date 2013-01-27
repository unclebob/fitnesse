package fitnesse.testsystems.slim.results;

public interface Result {

// Proposed interface:
//  String getActual();
//  String getExpected();
//  ExecutionResult getExecutionResult();

  /**
   * @return String representation of the response.
   * @deprecated Formatting should move to the frontend, based on reponse content.
   */
  @Deprecated
  String toHtml();
}
