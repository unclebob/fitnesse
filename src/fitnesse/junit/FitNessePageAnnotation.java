package fitnesse.junit;

import java.lang.annotation.Annotation;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;

/**
 * Annotation used to pass FitNesse page information to test listeners.
 */
public class FitNessePageAnnotation implements Annotation {
  private final TestPage testPage;
  private final WikiPage wikiPage;

  /**
   * Creates new.
   *
   * @param wikiPage page describing test.
   */
  public FitNessePageAnnotation(WikiPage wikiPage) {
    this.testPage = null;
    this.wikiPage = wikiPage;
  }

  /**
   * Creates new.
   *
   * @param testPage page describing test.
   */
  public FitNessePageAnnotation(TestPage testPage) {
    this.testPage = testPage;
    if (testPage instanceof WikiTestPage) {
      wikiPage = ((WikiTestPage) testPage).getSourcePage();
    } else {
      this.wikiPage = null;
    }
  }

  /**
   * @return wiki page for current test.
   */
  public WikiPage getWikiPage() {
    return wikiPage;
  }

  /**
   * @return current test page.
   */
  public TestPage getTestPage() {
    return testPage;
  }

  @Override
  public Class<? extends FitNessePageAnnotation> annotationType() {
    return FitNessePageAnnotation.class;
  }
}
