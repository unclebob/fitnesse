package fitnesse.testrunner;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.TestPage;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PagesByTestSystemTest{
  private WikiPage suite;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    suite = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("SuitePage"), "This is the test suite\n");
  }

  @Test
  public void testPagesForTestSystemAreSurroundedBySuiteSetupAndTeardown() throws Exception {
    WikiPage root = context.getRootPage();
    WikiPage testPage = addTestPage(suite, "TestOne", "My test");
    WikiPage slimPage = addTestPage(suite, "AaSlimTest", "!define TEST_SYSTEM {slim}\n" +
            "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
            "|string|get string arg?|\n" +
            "|wow|wow|\n");
    WikiPage setUp = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

    LinkedList<WikiPage> testPages = new LinkedList<>();
    testPages.add(setUp);
    testPages.add(slimPage);
    testPages.add(testPage);
    testPages.add(tearDown);

    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(testPages, context.getRootPage());
    Collection<WikiPageIdentity> descriptors = pagesByTestSystem.identities();
    WikiPageIdentity fitDescriptor = new WikiPageIdentity(testPage);
    WikiPageIdentity slimDescriptor = new WikiPageIdentity(slimPage);

    assertTrue(descriptors.contains(fitDescriptor));
    assertTrue(descriptors.contains(slimDescriptor));

    List<TestPage> fitList = pagesByTestSystem.testPagesForIdentity(fitDescriptor);
    List<TestPage> slimList = pagesByTestSystem.testPagesForIdentity(slimDescriptor);

    assertEquals(3, fitList.size());
    assertEquals(3, slimList.size());

    assertEquals(setUp, ((WikiTestPage) fitList.get(0)).getSourcePage());
    assertEquals(testPage, ((WikiTestPage) fitList.get(1)).getSourcePage());
    assertEquals(tearDown, ((WikiTestPage) fitList.get(2)).getSourcePage());

    assertEquals(setUp, ((WikiTestPage) slimList.get(0)).getSourcePage());
    assertEquals(slimPage, ((WikiTestPage) slimList.get(1)).getSourcePage());
    assertEquals(tearDown, ((WikiTestPage) slimList.get(2)).getSourcePage());
  }

  private WikiPage addTestPage(WikiPage page, String name, String content) {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

}
