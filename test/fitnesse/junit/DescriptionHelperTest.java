package fitnesse.junit;

import java.util.Arrays;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.Test;
import org.junit.runner.Description;

import static org.junit.Assert.*;

public class DescriptionHelperTest {
  private DescriptionFactory descriptionFactory = new DescriptionFactory();

  @Test
  public void testNormalDescription() {
    Description description = Description.EMPTY;
    WikiPage pageFound = DescriptionHelper.getWikiPage(description);

    assertNull(pageFound);
  }

  @Test
  public void testGetWikiPageNoPageData() {
    WikiPage page = mockWikiTestPage().getSourcePage();
    Description desc = descriptionFactory.createDescription(getClass(), page);

    WikiPage pageFound = DescriptionHelper.getWikiPage(desc);
    assertSame(page, pageFound);

    assertEquals(0, DescriptionHelper.getPageTags(pageFound).size());
  }

  @Test
  public void testGetWikiPageWithTags() {
    WikiPage page = mockWikiTestPage().getSourcePage();
    page.getData().getProperties().set(PageData.PropertySUITES, " First , Second, Third,Fourth,Fifth ");
    Description desc = descriptionFactory.createDescription(getClass(), page);

    WikiPage pageFound = DescriptionHelper.getWikiPage(desc);
    assertSame(page, pageFound);

    assertEquals(Arrays.asList("First", "Second", "Third", "Fourth", "Fifth"),
                  DescriptionHelper.getPageTags(pageFound));
  }

  private WikiTestPage mockWikiTestPage() {
    return JUnitRunNotifierResultsListenerTest.mockWikiTestPage();
  }
}
