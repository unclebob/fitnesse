package fitnesse.responders.templateUtilities;

import fitnesse.wiki.PathParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PageTitleTest {
  @Test
  public void defaultPageTitleHasNoTitleLinkOrBreadCrumbs() throws Exception {
    PageTitle pt = new PageTitle();
    assertNull(pt.getTitle());
    assertNull(pt.getLink());
    assertNull(pt.getPageType());
    assertEquals(0, pt.getBreadCrumbs().size());
  }

  @Test
  public void pageTitleWithTypeButNoResourceHasTypeAndTitleButNoLinkOrBreadCrumbs() throws Exception {
    PageTitle pt = new PageTitle("Title");
    assertEquals("Title", pt.getTitle());
    assertNull(pt.getLink());
    assertEquals("Title", pt.getPageType());
    assertEquals(0, pt.getBreadCrumbs().size());
  }

  @Test
  public void oneElementPageNameHasLinkAndTitleButNoBreadCrumbs() throws Exception {
    PageTitle pt = new PageTitle(PathParser.parse("SimplePage"));
    assertEquals("SimplePage", pt.getTitle());
    assertEquals("SimplePage", pt.getLink());
    assertEquals(0, pt.getBreadCrumbs().size());
    assertNull(pt.getPageType());
  }

  @Test
  public void twoElementPageNameHasLinkTitleAndBreadCrumb() throws Exception {
    PageTitle pt = new PageTitle(PathParser.parse("ParentPage.ChildPage"));
    assertEquals("ChildPage", pt.getTitle());
    assertEquals("ParentPage.ChildPage", pt.getLink());
    assertEquals(1, pt.getBreadCrumbs().size());
    PageTitle.BreadCrumb crumb = pt.getBreadCrumbs().get(0);
    assertEquals("ParentPage", crumb.getName());
    assertEquals("ParentPage", crumb.getLink());
  }

  @Test
  public void threeElementPagenameHasLinkTitleAndBreadCrumbs() throws Exception {
    PageTitle pt = new PageTitle(PathParser.parse("ParentPage.ChildPage.GrandChildPage"));
    assertEquals("GrandChildPage", pt.getTitle());
    assertEquals("ParentPage.ChildPage.GrandChildPage", pt.getLink());
    assertEquals(2, pt.getBreadCrumbs().size());
    PageTitle.BreadCrumb crumb = pt.getBreadCrumbs().get(0);
    assertEquals("ParentPage", crumb.getName());
    assertEquals("ParentPage", crumb.getLink());
    crumb = pt.getBreadCrumbs().get(1);
    assertEquals("ChildPage", crumb.getName());
    assertEquals("ParentPage.ChildPage", crumb.getLink());
  }

  @Test
  public void pageTitleWithPathAndTypeHasAllElements() throws Exception {
    PageTitle pt = new PageTitle("type", PathParser.parse("ParentPage.ChildPage"));
    assertEquals("ChildPage", pt.getTitle());
    assertEquals("ParentPage.ChildPage", pt.getLink());
    assertEquals(1, pt.getBreadCrumbs().size());
    PageTitle.BreadCrumb crumb = pt.getBreadCrumbs().get(0);
    assertEquals("ParentPage", crumb.getName());
    assertEquals("ParentPage", crumb.getLink());
    assertEquals("type", pt.getPageType());    
  }
}
