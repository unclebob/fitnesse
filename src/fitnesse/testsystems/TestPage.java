package fitnesse.testsystems;

public interface TestPage {

  String getName();

  String getFullPath();

  String getVariable(String name);

  ClassPath getClassPath();

  /**
   * Return the plain (wiki-) text. This function can be used by test systems (such as Cucumber and JBehave)
   * that do deal with the raw page content.
   *
   * @return Raw wiki content of the page.
   */
  String getContent();

  /**
   * Returns the complete, decorated HTML page, as shown in the web interface and as it is used by Fit and Slim.
   *
   * @return HTML output of the page.
   */
  String getHtml();
}
