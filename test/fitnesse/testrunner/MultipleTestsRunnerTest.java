// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import java.io.Closeable;
import java.io.IOException;
import fitnesse.FitNesseContext;
import fitnesse.testsystems.*;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

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
  public void shouldExecuteTestPagesGroupedByTestSystem() throws TestExecutionException, IOException {
    WikiPage testPage1 = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    WikiPage testPage2 = addTestPage(suite, "TestPage2", "!define TEST_SYSTEM {B}");

    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(asList(testPage1, testPage2), context.getRootPage());
    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, testSystemFactory);

    runner.executeTestPages();

    verify(testSystemFactory).create(forTestSystem("B"));
    verify(testSystemFactory).create(forTestSystem("A"));
  }

  @Test
  public void shouldCallCloseOnClosableTestSystemListener() throws TestExecutionException, IOException {
    WikiPage testPage = addTestPage(suite, "TestPage1", "!define TEST_SYSTEM {A}");
    ClosableTestSystemListener listener = mock(ClosableTestSystemListener.class);

    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem(asList(testPage), context.getRootPage());
    MultipleTestsRunner runner = new MultipleTestsRunner(pagesByTestSystem, testSystemFactory);
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

  private Descriptor forTestSystem(String type) {
    return argThat(new ForTestSystem(type));
  }

  class ForTestSystem extends ArgumentMatcher<Descriptor> {

    private final String testSystemType;

    public ForTestSystem(String testSystemType) {
      this.testSystemType = testSystemType;
    }
    @Override
    public boolean matches(Object descriptor) {
      return testSystemType.equals(((Descriptor) descriptor).getTestSystemType());
    }
  }

  static interface ClosableTestSystemListener extends TestSystemListener, Closeable {
  }

}
