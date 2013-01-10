package fitnesse.testsystems.slim.results;

public interface Result {

  /**
   * @return String representation of the response.
   * @deprecated Formatting should move to the frontend, based on reponse content.
   */
  String toHtml();
}
