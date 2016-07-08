package fitnesse.junit;

import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;
import org.junit.runner.Description;

/**
 * Factory to create jUnit test Descriptions.
 */
public class DescriptionFactory {
  /**
   * Creates Description for suite.
   * @param clazz class defining suite.
   * @return description.
   */
  public Description createSuiteDescription(Class<?> clazz) {
    return Description.createSuiteDescription(clazz);
  }

  /**
   * Creates description for a wiki page being run from a jUnit class.
   *
   * @param clazz class triggering page.
   * @param page  page to be executed.
   * @return description.
   */
  public Description createDescription(Class<?> clazz, WikiPage page) {
    String name = page.getPageCrawler().getFullPath().toString();
    return createDescription(clazz, name);
  }

  /**
   * Creates description for a wiki page being run from a jUnit class.
   *
   * @param clazz class triggering page.
   * @param page  page to be executed.
   * @return description.
   */
  public Description createDescription(Class<?> clazz, TestPage page) {
    String name = page.getFullPath();
    return createDescription(clazz, name);
  }

  private Description createDescription(Class<?> clazz, String name) {
    return Description.createTestDescription(clazz, name);
  }
}
