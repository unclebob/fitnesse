package fitnesse.junit;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;
import org.junit.Test;
import org.junit.runner.Description;

import static org.junit.Assert.*;

public class DescriptionFactoryTest {
  private DescriptionFactory descriptionFactory = new DescriptionFactory();

  @Test
  public void testCreateWithWikiTestPage() {
    WikiTestPage page = mockWikiTestPage();
    Description desc = descriptionFactory.createDescription(getClass(), page);

    assertNotNull(desc);
    assertEquals("WikiPage(fitnesse.junit.DescriptionFactoryTest)", desc.getDisplayName());

    TestPage pageFound = DescriptionHelper.getTestPage(desc);
    assertSame(page, pageFound);

    WikiPage wikiPageFound = DescriptionHelper.getWikiPage(desc);
    assertSame(page.getSourcePage(), wikiPageFound);
  }

  @Test
  public void testCreateWithWikiPage() {
    WikiPage page = mockWikiTestPage().getSourcePage();
    Description desc = descriptionFactory.createDescription(getClass(), page);

    assertNotNull(desc);
    assertEquals("WikiPage(fitnesse.junit.DescriptionFactoryTest)", desc.getDisplayName());
  }

  private WikiTestPage mockWikiTestPage() {
    return JUnitRunNotifierResultsListenerTest.mockWikiTestPage();
  }
}
