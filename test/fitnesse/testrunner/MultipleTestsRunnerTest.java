// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.run.TestRun;
import fitnesse.testrunner.run.TestRunFactoryRegistry;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.Closeable;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultipleTestsRunnerTest {
  private WikiPage suite;
  private FitNesseContext context;

  private TestSystemFactory testSystemFactory;

  @Before
  public void setUp() throws Exception {
    testSystemFactory = mock(TestSystemFactory.class);
    TestSystem testSystem = mock(TestSystem.class);
    when(testSystemFactory.create(any(Descriptor.class))).thenReturn(testSystem);

    context = FitNesseUtil.makeTestContext();
    suite = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("SuitePage"), "This is the test suite\n");
  }

  @Test
  public void shouldExecuteTestPagesGroupedByTestSystem() throws TestExecutionException {
    WikiPage testPage1 = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    WikiPage testPage2 = addTestPage(suite, "TestPage2", "!define TEST_SYSTEM {B}");

    MultipleTestsRunner runner = new MultipleTestsRunner(createRun(testPage1, testPage2), testSystemFactory);

    runner.executeTestPages();

    verify(testSystemFactory).create(forTestSystem("B"));
    verify(testSystemFactory).create(forTestSystem("A"));
  }

  @Test
  public void shouldCallCloseOnClosableTestSystemListener() throws TestExecutionException, IOException {
    WikiPage testPage = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    ClosableTestSystemListener listener = mock(ClosableTestSystemListener.class);

    MultipleTestsRunner runner = new MultipleTestsRunner(createRun(testPage), testSystemFactory);
    runner.addTestSystemListener(listener);
    runner.executeTestPages();

    verify(listener).close();
  }

  private WikiPage addTestPage(WikiPage page, String name, String content) {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

  private TestRun createRun(WikiPage... pages) {
    return TestRunFactoryRegistry.DEFAULT.createRun(asList(pages));
  }

  private Descriptor forTestSystem(String type) {
    return argThat(new ForTestSystem(type));
  }

  class ForTestSystem implements ArgumentMatcher<Descriptor> {

    private final String testSystemType;

    public ForTestSystem(String testSystemType) {
      this.testSystemType = testSystemType;
    }

    @Override
    public boolean matches(Descriptor descriptor) {
      return testSystemType.equals(descriptor.getTestSystemType());
    }
  }

  public interface ClosableTestSystemListener extends TestSystemListener, Closeable {
  }

}
