package fitnesse.junit;

import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;
import org.junit.runner.Description;

import java.lang.annotation.Annotation;

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
    String name = page.getFullPath().toString();
    FitNessePageAnnotation wikiPageAnnotation = new FitNessePageAnnotation(page);
    return createDescription(clazz, name, wikiPageAnnotation);
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
    FitNessePageAnnotation wikiPageAnnotation = new FitNessePageAnnotation(page);
    return createDescription(clazz, name, wikiPageAnnotation);
  }

  private Description createDescription(Class<?> clazz, String name, Annotation... annotations) {
    return Description.createTestDescription(clazz, name, annotations);
  }
}
