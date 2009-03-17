// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.List;

import junit.framework.TestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.SimpleCachinePage;

public class VirtualCouplingExtensionTest extends TestCase {
  public WikiPage root;
  public BaseWikiPage page1;
  public WikiPage page2;
  private PageCrawler crawler;

  @Override
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    FitNesseUtil.startFitnesse(root);

    page2 = crawler.addPage(root, PathParser.parse("PageTwo"), "page two");
    crawler.addPage(page2, PathParser.parse("PageTwoChild"), "page two child");
    page1 = (BaseWikiPage) crawler.addPage(root, PathParser.parse("PageOne"), "page one content\n!contents\n");
    crawler.addPage(page1, PathParser.parse("SomeOtherPage"), "some other page");

    setVirtualWiki(page1, "http://localhost:" + FitNesseUtil.port + "/PageTwo");
  }

  public static void setVirtualWiki(WikiPage page, String virtualWikiURL) throws Exception {
    PageData data = page.getData();
    data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, virtualWikiURL);
    page.commit(data);
  }

  @Override
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  public void testGetChildren() throws Exception {
    List<?> children = page1.getChildren();
    assertEquals(1, children.size());
    assertEquals("SomeOtherPage", ((WikiPage) children.get(0)).getName());

    VirtualCouplingExtension extension = (VirtualCouplingExtension) page1.getExtension(VirtualCouplingExtension.NAME);
    children = extension.getVirtualCoupling().getChildren();
    assertEquals(1, children.size());
    assertTrue(children.get(0) instanceof ProxyPage);
    assertEquals("PageTwoChild", ((WikiPage) children.get(0)).getName());
  }

  public void testNewProxyChildrenAreFound() throws Exception {
    CachingPage.cacheTime = 0;
    BaseWikiPage realChild = (BaseWikiPage) page2.getChildPage("PageTwoChild");

    PageCrawler crawler = page2.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    ProxyPage childProxy = (ProxyPage) crawler.getPage(page1, PathParser.parse("PageTwoChild"));
    assertNull(childProxy.getChildPage("AnotherChild"));

    crawler.addPage(realChild, PathParser.parse("AnotherChild"), "another child");
    assertNotNull(childProxy.getChildPage("AnotherChild"));
  }

  public void testProxyChildrenAreFoundOnStartUp() throws Exception {
    WikiPage page3 = crawler.addPage(root, PathParser.parse("PageThree"), "page three content");
    setVirtualWiki(page3, "http://localhost:" + FitNesseUtil.port + "/PageTwo");

    assertTrue(page3.hasExtension(VirtualCouplingExtension.NAME));

    VirtualCouplingExtension extension = (VirtualCouplingExtension) page3.getExtension(VirtualCouplingExtension.NAME);
    List<?> children = extension.getVirtualCoupling().getChildren();
    assertEquals(1, children.size());
    assertEquals("PageTwoChild", ((WikiPage) children.get(0)).getName());
  }

  public void testGetChildrenOnlyAsksOnce() throws Exception {
    CachingPage.cacheTime = 10000;
    ProxyPage.retrievalCount = 0;
    SimpleCachinePage page = new SimpleCachinePage("RooT", null);
    setVirtualWiki(page, "http://localhost:" + FitNesseUtil.port + "/PageTwo");
    VirtualCouplingExtension extension = (VirtualCouplingExtension) page.getExtension(VirtualCouplingExtension.NAME);
    extension.getVirtualCoupling().getChildren();
    assertEquals(1, ProxyPage.retrievalCount);
  }

  public void testNoNastyExceptionIsThrownWhenVirutalChildrenAreLoaded() throws Exception {
    WikiPage page3 = crawler.addPage(root, PathParser.parse("PageThree"), "page three content");
    setVirtualWiki(page3, "http://google.com/SomePage");
    VirtualCouplingExtension extension = (VirtualCouplingExtension) page3.getExtension(VirtualCouplingExtension.NAME);
    extension.getVirtualCoupling().getChildren();
    assertNotNull(page3.getChildPage("VirtualWikiNetworkError"));
  }

}
