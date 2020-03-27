package fitnesse.wiki;

import fit.exception.IncorrectPathException;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WikiWordReferenceTest {

  private WikiPage referer;

  @Before
  public void makePageStructure() {
    WikiPage root2 = InMemoryPage.makeRoot("RooT");
    WikiPage top = addPage(root2, "TopPage");
    WikiPage target = addPage(top, "TargetPage");
    referer = addPage(target, "ReferingPage");
    addPage(target, "SubTarget");
  }

  @Test
  public void testBackwardSearch() {
    String actual = WikiWordReference.expandPrefix(referer, "<TargetPage.SubTarget");
    assertEquals(".TopPage.TargetPage.SubTarget", actual);
  }

  @Test
  public void ifPageDoesNotExist() {
    String actual = WikiWordReference.expandPrefix(referer, "<NoSuchPage");
    assertEquals(".NoSuchPage", actual);
  }

  @Test
  public void ifPathIsEmpty() {
    try {
      WikiWordReference.expandPrefix(referer, "");
    } catch (IncorrectPathException e) {
      assertEquals("Path to Page must be filled in", e.getMessage());
    }
  }

  @Test
  public void testHtmlRendering() {
    PageData data = referer.getData();
    data.setContent("<TargetPage.SubTarget");
    referer.commit(data);
    String renderedLink = referer.getHtml();
    assertEquals("<a href=\"TopPage.TargetPage.SubTarget\">&lt;TargetPage.SubTarget</a>", renderedLink);
  }

  private WikiPage addPage(WikiPage parent, String childName) {
    return WikiPageUtil.addPage(parent, PathParser.parse(childName), "");
  }
}
