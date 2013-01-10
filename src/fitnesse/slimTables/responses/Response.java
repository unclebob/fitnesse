package fitnesse.slimTables.responses;

public interface Response {

  /**
   * @return String representation of the response.
   * @deprecated Formatting should move to the frontend, based on reponse content.
   */
  String toHtml();
}
