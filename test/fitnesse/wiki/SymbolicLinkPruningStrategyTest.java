package fitnesse.wiki;

import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SymbolicLinkPruningStrategyTest {
  private WikiPage root;

  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("RooT");
    WikiPage linkingPage = WikiPageUtil.addPage(root, PathParser.parse("LinkingPage"), "");
    WikiPage linkedPage = WikiPageUtil.addPage(root, PathParser.parse("LinkedPage"), "");
    WikiPageUtil.addPage(linkedPage, PathParser.parse("ChildPage"), "");
    WikiPageUtil.addPage(root, PathParser.parse("OtherPage"), "");
    addSymbolicLink(linkingPage, "SymLink", ".LinkedPage");
    addSymbolicLink(linkedPage, "InnerLink", ".OtherPage");
  }

  @Test
  public void leavesOutSymbolicLinks() {
    assertEquals(asList("", "LinkedPage", "LinkedPage.ChildPage", "LinkingPage", "OtherPage"),
      traverse(root, new SymbolicLinkPruningStrategy()));
  }

  @Test
  public void followsSymbolicLinksWithoutPruning() {
    assertEquals(asList("", "LinkedPage", "LinkedPage.ChildPage", "LinkedPage.InnerLink", "LinkingPage",
        "LinkingPage.SymLink", "LinkingPage.SymLink.ChildPage", "LinkingPage.SymLink.InnerLink", "OtherPage"),
      traverse(root, new NoPruningStrategy()));
  }

  @Test
  public void traversesSymbolicLinkItStartsAtButNoSymbolicLinksBelow() {
    WikiPage symLink = root.getChildPage("LinkingPage").getChildPage("SymLink");
    assertEquals(asList("LinkingPage.SymLink", "LinkingPage.SymLink.ChildPage"),
      traverse(symLink, new SymbolicLinkPruningStrategy()));
  }

  private List<String> traverse(WikiPage startPage, PagePruningStrategy strategy) {
    List<String> paths = new ArrayList<>();
    startPage.getPageCrawler().traverse(page -> paths.add(page.getFullPath().toString()), strategy);
    paths.sort(null);
    return paths;
  }

  private void addSymbolicLink(WikiPage page, String linkName, String linkPath) {
    PageData data = page.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set(linkName, linkPath);
    page.commit(data);
  }
}
