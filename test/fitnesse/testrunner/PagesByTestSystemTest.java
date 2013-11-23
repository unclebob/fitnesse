package fitnesse.testrunner;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.Descriptor;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PagesByTestSystemTest{
  private WikiPage root;
  private WikiPage suite;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    suite = WikiPageUtil.addPage(root, PathParser.parse("SuitePage"), "This is the test suite\n");
  }

  @Test
  public void testPagesForTestSystemAreSurroundedBySuiteSetupAndTeardown() throws Exception {
    WikiPage testPage = addTestPage(suite, "TestOne", "My test");
    WikiPage slimPage = addTestPage(suite, "AaSlimTest", "!define TEST_SYSTEM {slim}\n" +
            "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
            "|string|get string arg?|\n" +
            "|wow|wow|\n");
    WikiPage setUp = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

    LinkedList<WikiPage> testPages = new LinkedList<WikiPage>();
    testPages.add(setUp);
    testPages.add(slimPage);
    testPages.add(testPage);
    testPages.add(tearDown);

    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(testPages, context.root, new StubDescriptorFactory());
    Collection<Descriptor> descriptors = pagesByTestSystem.descriptors();
    Descriptor fitDescriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, false, "");
    Descriptor slimDescriptor = new WikiPageDescriptor(slimPage.readOnlyData(), false, false, "");

    assertTrue(descriptors.contains(fitDescriptor));
    assertTrue(descriptors.contains(slimDescriptor));

    List<WikiPage> fitList = pagesByTestSystem.testPageForDescriptor(fitDescriptor);
    List<WikiPage> slimList = pagesByTestSystem.testPageForDescriptor(slimDescriptor);

    assertEquals(3, fitList.size());
    assertEquals(3, slimList.size());

    assertEquals(setUp, fitList.get(0));
    assertEquals(testPage, fitList.get(1));
    assertEquals(tearDown, fitList.get(2));

    assertEquals(setUp, slimList.get(0));
    assertEquals(slimPage, slimList.get(1));
    assertEquals(tearDown, slimList.get(2));
  }

  private WikiPage addTestPage(WikiPage page, String name, String content) {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

  static private class StubDescriptorFactory implements PagesByTestSystem.DescriptorFactory {

    @Override
    public Descriptor create(WikiPage page) {
      return new WikiPageDescriptor(page.getData(), false, false, "");
    }
  }


}
