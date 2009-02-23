//package fitnesse.responders.run;
//
//import static fitnesse.testutil.RegexTestCase.assertSubString;
//import static org.junit.Assert.assertEquals;
//
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.Test;
//
//import fitnesse.wiki.PathParser;
//import fitnesse.wiki.WikiPage;
//
//public class MultipleTestsRunnerTest {
//  
//  @Test
//  public void testBuildClassPath() throws Exception {
//    responder.page = suite;
//    List<WikiPage> testPages = SuiteResponder.getAllTestPagesUnder(suite);
//    String classpath = SuiteResponder.buildClassPath(testPages, responder.page);
//    assertSubString("classes", classpath);
//    assertSubString("dummy.jar", classpath);
//  }
//  
//  @Test
//  public void testGenerateSuiteMapWithMultipleTestSystems() throws Exception {
//    WikiPage slimPage = addTestToSuite("SlimTest", simpleSlimDecisionTable);
//    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = SuiteResponder.makeMapOfPagesByTestSystem(suite, root, null);
//
//    TestSystem.Descriptor fitDescriptor = TestSystem.getDescriptor(testPage.getData());
//    TestSystem.Descriptor slimDescriptor = TestSystem.getDescriptor(slimPage.getData());
//    List<WikiPage> fitList = map.get(fitDescriptor);
//    List<WikiPage> slimList = map.get(slimDescriptor);
//
//    assertEquals(1, fitList.size());
//    assertEquals(1, slimList.size());
//    assertEquals(testPage, fitList.get(0));
//    assertEquals(slimPage, slimList.get(0));
//  }
//
//  @Test
//  public void testPagesForTestSystemAreSurroundedBySuiteSetupAndTeardown() throws Exception {
//    WikiPage slimPage = addTestToSuite("SlimTest", simpleSlimDecisionTable);
//    WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
//    WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");
//
//    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = SuiteResponder.makeMapOfPagesByTestSystem(suite, root, null);
//    TestSystem.Descriptor fitDescriptor = TestSystem.getDescriptor(testPage.getData());
//    TestSystem.Descriptor slimDescriptor = TestSystem.getDescriptor(slimPage.getData());
//
//    List<WikiPage> fitList = map.get(fitDescriptor);
//    List<WikiPage> slimList = map.get(slimDescriptor);
//
//    assertEquals(3, fitList.size());
//    assertEquals(3, slimList.size());
//
//    assertEquals(setUp, fitList.get(0));
//    assertEquals(testPage, fitList.get(1));
//    assertEquals(tearDown, fitList.get(2));
//
//    assertEquals(setUp, slimList.get(0));
//    assertEquals(slimPage, slimList.get(1));
//    assertEquals(tearDown, slimList.get(2));
//  }
//}
