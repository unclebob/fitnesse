package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.List;

import static fitnesse.wiki.PageData.SUITE_SETUP_NAME;
import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;
import static org.junit.Assert.assertEquals;

public abstract class PageListSetUpTearDownTestBase extends PageListTestBase {

  @Test
  public void testSetUpAndTearDown() {
    WikiPage setUp = addChildPage(root, SUITE_SETUP_NAME, "suite set up");
    WikiPage tearDown = addChildPage(root, SUITE_TEARDOWN_NAME, "suite tear down");

    List<WikiPage> testPages = addSuiteSetUpsAndTearDowns(makeTestPageList());
    assertEquals(3, testPages.size());
    assertEquals(setUp, testPages.get(0));
    assertEquals(testPage, testPages.get(1));
    assertEquals(tearDown, testPages.get(2));
  }

  protected abstract List<WikiPage> addSuiteSetUpsAndTearDowns(List<WikiPage> pages);
}
