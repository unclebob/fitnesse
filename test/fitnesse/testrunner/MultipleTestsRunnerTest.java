// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class MultipleTestsRunnerTest {
  private WikiPage root;
  private WikiPage suite;
  private FitNesseContext context;

  private TestingTracker testingTracker;
  private TestSystemFactory testSystemFactory;
  private TestSystem testSystem;

  @Before
  public void setUp() throws Exception {
    testingTracker = mock(TestingTracker.class);
    testSystemFactory = mock(TestSystemFactory.class);
    testSystem = mock(TestSystem.class);
    when(testSystemFactory.create(any(Descriptor.class))).thenReturn(testSystem);

    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    suite = WikiPageUtil.addPage(root, PathParser.parse("SuitePage"), "This is the test suite\n");
 }

  @Test
  public void shouldExecuteTestPagesGroupedByTestSystem() throws IOException, InterruptedException {
    WikiPage testPage1 = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    WikiPage testPage2 = addTestPage(suite, "TestPage2", "!define TEST_SYSTEM {B}");

    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(asList(testPage1, testPage2), context.root);
    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, testingTracker, testSystemFactory);

    runner.executeTestPages();

    verify(testSystemFactory).create(forTestSystem("B"));
    verify(testSystemFactory).create(forTestSystem("A"));
  }

  @Test
  public void shouldCallCloseOnClosableTestSystemListener() throws IOException, InterruptedException {
    WikiPage testPage = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    ClosableTestSystemListener listener = mock(ClosableTestSystemListener.class);

    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(asList(testPage), context.root);
    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, testingTracker, testSystemFactory);
    runner.addTestSystemListener(listener);
    runner.executeTestPages();

    verify(listener).close();
  }

  @Test
  public void callsTestingTrackerBeforeAndAfterTestExecution() throws IOException, InterruptedException {
    final String stopId = "42";
    WikiPage testPage = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    ClosableTestSystemListener listener = mock(ClosableTestSystemListener.class);
    when(testingTracker.addStartedProcess(any(Stoppable.class))).thenReturn(stopId);

    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(asList(testPage), context.root);
    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, testingTracker, testSystemFactory);
    runner.addTestSystemListener(listener);
    runner.executeTestPages();

    verify(testingTracker).addStartedProcess(runner);
    verify(testingTracker).removeEndedProcess(stopId);
  }

  private WikiPage addTestPage(WikiPage page, String name, String content) {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

  private MultipleTestsRunner newTestRunnerWithListener(TestSystemListener listener) {
    WikiPage testPage = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem((List) asList(testPage), context.root);
    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, testingTracker, testSystemFactory);
    runner.addTestSystemListener(listener);
    return runner;
  }


  private Descriptor forTestSystem(String type) {
    return argThat(new ForTestSystem(type));
  }

  class ForTestSystem extends ArgumentMatcher<Descriptor> {

    private final String testSystemType;

    public ForTestSystem(String testSystemType) {
      this.testSystemType = testSystemType;
    }
    public boolean matches(Object descriptor) {
      return testSystemType.equals(((Descriptor) descriptor).getTestSystemType());
    }
  }

  static interface ClosableTestSystemListener extends TestSystemListener, Closeable {
  }

}
