package util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.http.MockRequest;
import fitnesse.responders.editing.EditResponder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TemplateUtilTest {
  
  private static final String REGULAR_PARENT_PATH = ".LibraryParent.TemplateLibrary.TemplateOne";
  private static final String ROOT_PARENT_PATH = ".TemplateLibrary.TemplateFromRoot";
  
  private WikiPage root;
  private MockRequest request;
  private EditResponder responder;
  private PageCrawler crawler;

  @Before public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    FitNesseUtil.makeTestContext(root);
    crawler = root.getPageCrawler();
    request = new MockRequest();
    responder = new EditResponder();
  }
  
  @Test public void testGetTemplatesFromUncles() {
    crawler.addPage(root, PathParser.parse(".TemplateLibrary"), "template library");
    crawler.addPage(root, PathParser.parse(".TemplateLibrary.TemplateFromRoot"), "template from root");
    
    crawler.addPage(root, PathParser.parse(".LibraryParent"), "library parent");
    crawler.addPage(root, PathParser.parse(".LibraryParent.TemplateLibrary"), "template library 2");
    crawler.addPage(root, PathParser.parse(".LibraryParent.TemplateLibrary.TemplateOne"), "template 1");
    
    WikiPage childPage = crawler.addPage(root, PathParser.parse(".LibraryParent.ChildPage"), "library parent");
    
    List<String> pathList = TemplateUtil.getTemplatesFromUncles(childPage);
    
    assertTrue(pathList.contains(REGULAR_PARENT_PATH));
    assertTrue(pathList.contains(ROOT_PARENT_PATH));
  }
  
  @Test public void testGetTemplatesFromUnclesDoesntTakeTemplatesChildren() {
    crawler.addPage(root, PathParser.parse(".TemplateLibrary"), "template library");
    crawler.addPage(root, PathParser.parse(".TemplateLibrary.TemplateFromRoot"), "template from root");
    crawler.addPage(root, PathParser.parse(".TemplateLibrary.TemplateFromRoot.TemplateFromRootChild"), "template from root child");
    
    crawler.addPage(root, PathParser.parse(".LibraryParent"), "library parent");
    WikiPage childPage = crawler.addPage(root, PathParser.parse(".LibraryParent.ChildPage"), "library parent");
    
    List<String> pathList = TemplateUtil.getTemplatesFromUncles(childPage);
    
    assertTrue(pathList.contains(ROOT_PARENT_PATH));
    assertFalse(pathList.contains(ROOT_PARENT_PATH + ".TemplateFromRootChild"));
  }

  @Test public void testGetShortTemplateName() {
    String parsed = TemplateUtil.getShortTemplateName(REGULAR_PARENT_PATH);
    assertEquals("LibraryParent._.TemplateOne", parsed);
    
    String parsed2 = TemplateUtil.getShortTemplateName(ROOT_PARENT_PATH);
    assertEquals("._.TemplateFromRoot", parsed2);
  }
  
  @Test public void testGetShortTemplateNames() {
    List<String> pathList = new ArrayList<String>();
    pathList.add(REGULAR_PARENT_PATH);
    pathList.add(ROOT_PARENT_PATH);
    
    Map<String, String> pathMap = TemplateUtil.getShortTemplateNames(pathList);
    assertEquals("LibraryParent._.TemplateOne", pathMap.get(REGULAR_PARENT_PATH));
    assertEquals("._.TemplateFromRoot", pathMap.get(ROOT_PARENT_PATH));
  }
  

}
