// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProxyPageTest {
  private WikiPage root;
  private WikiPage original;
  private ProxyPage proxy;
  public WikiPage child1;
  private PageCrawler crawler;
  private WikiPagePath page1Path;

  @Before
  public void setUp() throws Exception {
    ProxyPage.cacheTime = 0;
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
    proxy.setHostPort("localhost", FitNesseUtil.PORT);
  }

  @After
  public void tearDown() throws Exception {
    FitNesseUtil.stopFitnesse();
  }

  @Test
  public void testConstructor() throws Exception {
    assertEquals("page one content", proxy.getData().getContent());
    assertEquals("PageOne", proxy.getName());
    assertEquals(true, proxy.getData().hasAttribute("Attr1"));
  }

  @Test
  public void testHasChildren() throws Exception {
    assertEquals(false, proxy.hasChildPage("BlaH"));
    assertEquals(true, proxy.hasChildPage("ChildOne"));
    assertEquals(true, proxy.hasChildPage("ChildTwo"));
  }

  @Test
  public void testGetChildrenOneAtATime() throws Exception {
    WikiPage child1 = proxy.getChildPage("ChildOne");
    assertEquals("child one", child1.getData().getContent());
    WikiPage child2 = proxy.getChildPage("ChildTwo");
    assertEquals("child two", child2.getData().getContent());
  }

  @Test
  public void testGetAllChildren() throws Exception {
    List<?> children = proxy.getChildren();
    assertEquals(2, children.size());
    WikiPage child = (WikiPage) children.get(0);
    assertEquals(true, "ChildOne".equals(child.getName()) || "ChildTwo".equals(child.getName()));
    child = (WikiPage) children.get(1);
    assertEquals(true, "ChildOne".equals(child.getName()) || "ChildTwo".equals(child.getName()));
  }

  @Test
  public void testSetHostAndPort() throws Exception {
    List<?> children = proxy.getChildren();
    proxy.setHostPort("a.new.host", 123);

    assertEquals("a.new.host", proxy.getHost());
    assertEquals(123, proxy.getHostPort());

    for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
      ProxyPage page = (ProxyPage) iterator.next();
      assertEquals("a.new.host", page.getHost());
      assertEquals(123, page.getHostPort());
    }
  }

  @Test
  public void testCanFindNewChildOfAProxy() throws Exception {
    ProxyPage child1Proxy = (ProxyPage) proxy.getChildPage("ChildOne");
    assertNull(child1Proxy.getChildPage("ChildOneChild"));

    crawler.addPage(child1, PathParser.parse("ChildOneChild"), "child one child");

    assertNotNull(child1Proxy.getChildPage("ChildOneChild"));
  }

  @Test
  public void testHasSubpageCallsLoadChildrenNoMoreThanNeeded() throws Exception {
    proxy.getNormalChildren();
    ProxyPage.retrievalCount = 0;
    proxy.hasChildPage("ChildTwo");
    assertEquals(0, ProxyPage.retrievalCount);
    proxy.hasChildPage("SomeMissingChild");
    assertEquals(1, ProxyPage.retrievalCount);
  }

  @Test
  public void testGetVersions() {
    Collection<VersionInfo> versions = proxy.getVersions();
    assertNotNull(versions);
    assertEquals(3, versions.size());
  }


  @Test
  public void testPageDataIsCached() throws Exception {
    ProxyPage.cacheTime = 100;

    PageData data1 = proxy.getCachedData();
    PageData data2 = proxy.getCachedData();

    assertSame(data1, data2);
  }

}
