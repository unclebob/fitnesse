// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import fitnesse.testutil.FitNesseUtil;

public class ProxyPageTest extends TestCase {
  private WikiPage root;
  private WikiPage original;
  private ProxyPage proxy;
  public WikiPage child1;
  private PageCrawler crawler;
  private WikiPagePath page1Path;

  public void setUp() throws Exception {
    CachingPage.cacheTime = 0;
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    page1Path = PathParser.parse("PageOne");
    original = crawler.addPage(root, page1Path, "page one content");
    child1 = crawler.addPage(original, PathParser.parse("ChildOne"), "child one");
    crawler.addPage(original, PathParser.parse("ChildTwo"), "child two");
    PageData data = original.getData();
    data.setAttribute("Attr1");
    original.commit(data);

    FitNesseUtil.startFitnesse(root);

    proxy = new ProxyPage(original);
    proxy.setTransientValues("localhost", new Date().getTime());
    proxy.setHostPort(FitNesseUtil.port);
  }

  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  public void testConstructor() throws Exception {
    assertEquals("page one content", proxy.getData().getContent());
    assertEquals("PageOne", proxy.getName());
    assertEquals(true, proxy.getData().hasAttribute("Attr1"));
  }

  public void testHasChildren() throws Exception {
    assertEquals(false, proxy.hasChildPage("BlaH"));
    assertEquals(true, proxy.hasChildPage("ChildOne"));
    assertEquals(true, proxy.hasChildPage("ChildTwo"));
  }

  public void testGetChildrenOneAtATime() throws Exception {
    WikiPage child1 = proxy.getChildPage("ChildOne");
    assertEquals("child one", child1.getData().getContent());
    WikiPage child2 = proxy.getChildPage("ChildTwo");
    assertEquals("child two", child2.getData().getContent());
  }

  public void testGetAllChildren() throws Exception {
    List<?> children = proxy.getChildren();
    assertEquals(2, children.size());
    WikiPage child = (WikiPage) children.get(0);
    assertEquals(true, "ChildOne".equals(child.getName()) || "ChildTwo".equals(child.getName()));
    child = (WikiPage) children.get(1);
    assertEquals(true, "ChildOne".equals(child.getName()) || "ChildTwo".equals(child.getName()));
  }

  public void testSetHostAndPort() throws Exception {
    List<?> children = proxy.getChildren();
    proxy.setTransientValues("a.new.host", new Date().getTime());
    proxy.setHostPort(123);

    assertEquals("a.new.host", proxy.getHost());
    assertEquals(123, proxy.getHostPort());

    for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
      ProxyPage page = (ProxyPage) iterator.next();
      assertEquals("a.new.host", page.getHost());
      assertEquals(123, page.getHostPort());
    }
  }

  public void testCanFindNewChildOfAProxy() throws Exception {
    ProxyPage child1Proxy = (ProxyPage) proxy.getChildPage("ChildOne");
    assertNull(child1Proxy.getChildPage("ChildOneChild"));

    crawler.addPage(child1, PathParser.parse("ChildOneChild"), "child one child");
    assertNotNull(child1Proxy.getChildPage("ChildOneChild"));
  }

  public void testHasSubpageCallsLoadChildrenNoMoreThanNeeded() throws Exception {
    proxy.loadChildren();
    ProxyPage.retrievalCount = 0;
    proxy.hasChildPage("ChildTwo");
    assertEquals(0, ProxyPage.retrievalCount);
    proxy.hasChildPage("SomeMissingChild");
    assertEquals(1, ProxyPage.retrievalCount);
  }
}
