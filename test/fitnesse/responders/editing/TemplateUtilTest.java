package fitnesse.responders.editing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.fs.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TemplateUtilTest {

  private static final String REGULAR_PARENT_PATH = ".LibraryParent.TemplateLibrary.TemplateOne";
  private static final String ROOT_PARENT_PATH = ".TemplateLibrary.TemplateFromRoot";
  private static final String ROOT_OVERRIDDEN_PATH = ".TemplateLibrary.TemplateOne";

  private WikiPage root;

  @Before public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
  }

  @Test public void testGetTemplatesFromUncles() {
    WikiPageUtil.addPage(root, PathParser.parse(".TemplateLibrary"), "template library");
    WikiPageUtil.addPage(root, PathParser.parse(".TemplateLibrary.TemplateFromRoot"), "template from root");

    WikiPageUtil.addPage(root, PathParser.parse(".LibraryParent"), "library parent");
    WikiPageUtil.addPage(root, PathParser.parse(".LibraryParent.TemplateLibrary"), "template library 2");
    WikiPageUtil.addPage(root, PathParser.parse(".LibraryParent.TemplateLibrary.TemplateOne"), "template 1");

    WikiPage childPage = WikiPageUtil.addPage(root, PathParser.parse(".LibraryParent.ChildPage"), "library parent");

    List<String> pathList = TemplateUtil.getTemplatesFromUncles(childPage);

    assertTrue(pathList.contains(REGULAR_PARENT_PATH));
    assertTrue(pathList.contains(ROOT_PARENT_PATH));
  }

  @Test public void testGetTemplatesFromUnclesDoesntTakeTemplatesChildren() {
    WikiPageUtil.addPage(root, PathParser.parse(".TemplateLibrary"), "template library");
    WikiPageUtil.addPage(root, PathParser.parse(".TemplateLibrary.TemplateFromRoot"), "template from root");
    WikiPageUtil.addPage(root, PathParser.parse(".TemplateLibrary.TemplateFromRoot.TemplateFromRootChild"), "template from root child");

    WikiPageUtil.addPage(root, PathParser.parse(".LibraryParent"), "library parent");
    WikiPage childPage = WikiPageUtil.addPage(root, PathParser.parse(".LibraryParent.ChildPage"), "library parent");

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
    List<String> pathList = new ArrayList<>();
    pathList.add(REGULAR_PARENT_PATH);
    pathList.add(ROOT_PARENT_PATH);

    Map<String, String> pathMap = TemplateUtil.getShortTemplateNames(pathList);
    assertEquals(REGULAR_PARENT_PATH, pathMap.get("LibraryParent._.TemplateOne"));
    assertEquals(ROOT_PARENT_PATH, pathMap.get("._.TemplateFromRoot"));
  }

  @Test public void testGetPageNames() {
    List<String> pathList = new ArrayList<>();
    pathList.add(REGULAR_PARENT_PATH);
    pathList.add(ROOT_PARENT_PATH);

    Map<String, String> pathMap = TemplateUtil.getPageNames(pathList);
    assertEquals(REGULAR_PARENT_PATH, pathMap.get("Template One"));
    assertEquals(ROOT_PARENT_PATH, pathMap.get("Template From Root"));
  }

  @Test public void shouldShowOnlyOneTemplateWithASpecificName() {
    List<String> pathList = new ArrayList<>();
    pathList.add(REGULAR_PARENT_PATH);
    pathList.add(ROOT_PARENT_PATH);
    pathList.add(ROOT_OVERRIDDEN_PATH);

    Map<String, String> pathMap = TemplateUtil.getPageNames(pathList);
    assertEquals(2, pathMap.size());
    assertEquals(REGULAR_PARENT_PATH, pathMap.get("Template One"));
    assertEquals(ROOT_PARENT_PATH, pathMap.get("Template From Root"));
  }

}
