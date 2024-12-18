package fitnesse.reporting.history;

import fitnesse.FitNesseContext;
import fitnesse.reporting.history.TestExecutionReport.TestResult;
import fitnesse.reporting.history.TestXmlFormatter.WriterFactory;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.ExecutionLogListener;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import fitnesse.util.DateTimeUtil;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPageProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TestXmlFormatterTest {
  private static final String TEST_TIME = "4/13/2009 15:21:43";
  private DateAlteringClock clock;
  private FitNesseContext context;

  @Before
  public void setUp() throws ParseException {
    clock = new DateAlteringClock(DateTimeUtil.getDateFromString(TEST_TIME), TimeZone.getDefault()).freeze();
    context = FitNesseUtil.makeTestContext();
  }

  @After
  public void tearDown() {
    Clock.restoreDefaultClock();
  }

  @Test
  public void makeFileName() throws Exception {
    TestSummary summary = new TestSummary(1, 2, 3, 4);
    assertEquals(
            "20090413152143_1_2_3_4.xml",
            SuiteResponder.makeResultFileName(summary, clock.currentClockTimeInMillis()));
  }

  @Test
  public void processTestResultsShouldBuildUpCurrentResultAndFinalSummary() throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    WikiTestPage page = new WikiTestPage(new WikiPageDummy("name", "content", null));
    page.getData().setAttribute(PageData.PropertySUITES, "tag1");
    WriterFactory writerFactory = mock(WriterFactory.class);
    final TestResult testResult = new TestResult();
    TestXmlFormatter formatter = new TestXmlFormatter(context , page.getSourcePage(), writerFactory) {
      @Override
      protected TestResult newTestResult() {
        return testResult;
      }

      @Override
      protected void writeResults() throws IOException {
      }
    };
    final long startTime = clock.currentClockTimeInMillis();

    formatter.testOutputChunk(page, "outputChunk");

    formatter.testStarted(page);

    clock.elapse(27);

    TestSummary summary = new TestSummary(9,8,7,6);
    formatter.testComplete(page, summary);
    formatter.close();

    assertThat(formatter.testResponse.getFinalCounts(), equalTo(new TestSummary(0, 1, 0, 0)));
    assertThat(formatter.testResponse.getResults().size(), is(1));
    assertThat(formatter.testResponse.getResults().get(0), is(testResult));
    assertThat(testResult.startTime, is(startTime));
    assertThat(testResult.content, is("outputChunk"));
    assertThat(testResult.right, is("9"));
    assertThat(testResult.wrong, is("8"));
    assertThat(testResult.ignores, is("7"));
    assertThat(testResult.exceptions, is("6"));
    assertThat(testResult.runTimeInMillis, is("27"));
    assertThat(testResult.relativePageName, is(page.getName()));
    assertThat(testResult.tags, is("tag1"));
  }

  @Test
  public void allTestingCompleteShouldSetTotalRunTime() throws Exception {
    FitNesseContext context = mock(FitNesseContext.class);
    WikiPage page = new WikiPageDummy("name", "content", null);
    WriterFactory writerFactory = mock(WriterFactory.class);
    TestXmlFormatter formatter = new TestXmlFormatter(context , page, writerFactory) {
      @Override
      protected void writeResults() {
      }
    };

    formatter.testStarted(new WikiTestPage(page));
    clock.elapse(77L);
    formatter.close();
    assertThat(formatter.testResponse.getTotalRunTimeInMillis(), is(77L));
  }

  @Test
  public void allExecutionOutputShouldBeAddedToHistory() throws IOException, SAXException {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    WikiPage page = new WikiPageDummy("name", "content", null);
    final LinkedList<StringWriter> writers = new LinkedList<>();
    TestXmlFormatter formatter = new TestXmlFormatter(context, page, new WriterFactory() {
      @Override
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException {
        StringWriter w = new StringWriter();
        writers.add(w);
        return w;
      }
    });

    WikiTestPage testPage = new WikiTestPage(page);

    formatter.commandStarted(new ExecutionLogListener.ExecutionContext() {
      @Override
      public String getCommand() {
        return "commandLine";
      }

      @Override
      public String getTestSystemName() {
        return "testSystem";
      }
    });
    formatter.stdOut("Command started");
    formatter.testStarted(testPage);
    formatter.stdOut("After started");
    clock.elapse(77L);
    formatter.testComplete(testPage, new TestSummary(1, 2, 3, 4));
    formatter.exitCode(0);
    formatter.close();

    String output = writers.get(0).toString();
    Document document = XmlUtil.newDocument(output);
    Element testResultsElement = document.getDocumentElement();
    assertEquals("testResults", testResultsElement.getNodeName());
    Element executionLog = XmlUtil.getElementByTagName(testResultsElement, "executionLog");
    Element command = XmlUtil.getElementByTagName(executionLog, "command");
    Element stdOut = XmlUtil.getElementByTagName(executionLog, "stdOut");
    Element exitCode = XmlUtil.getElementByTagName(executionLog, "exitCode");

    assertEquals(output, "commandLine", command.getTextContent());
    assertEquals(output, "Command started\nAfter started\n", stdOut.getTextContent());
    assertEquals(output, "0", exitCode.getTextContent());
  }

  @Test
  public void executionReportExceptionsAreThreadSafe() throws IOException {
    final TestXmlFormatter formatter = getTestXmlFormatterWithDummyWriter();
    testThreadSaveOperation(formatter, new Runnable() {
      @Override
      public void run() {
        formatter.exceptionOccurred(new Exception("foo"));
      }
    });
  }

  @Test
  public void executionReportResultsAreThreadSafe() throws IOException {
    final TestXmlFormatter formatter = getTestXmlFormatterWithDummyWriter();
    testThreadSaveOperation(formatter, new Runnable() {
      @Override
      public void run() {
        formatter.testStarted(new WikiTestPage(new WikiPageDummy("name", "content", null)));
      }
    });
  }

  private void testThreadSaveOperation(TestXmlFormatter formatter, final Runnable target) throws IOException {
    final boolean[] sentinel = { true };
    Thread dataInjector = new Thread(new Runnable() {
      @Override
      public void run() {
        while (sentinel[0]) {
          target.run();
          Thread.yield();
        }
      }
    });

    dataInjector.setDaemon(true);
    dataInjector.start();
    try {
      Thread.yield();
      formatter.close();
    } finally {
      sentinel[0] = false;
    }
  }

  private TestXmlFormatter getTestXmlFormatterWithDummyWriter() {
    return new TestXmlFormatter(context, new WikiPageDummy("name", "content", null), new WriterFactory() {
      @Override
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws IOException {
        return new StringWriter();
      }
    });
  }

  @Test
  public void shouldNotCreateTestHistory() throws Exception {
    WikiTestPage page = new WikiTestPage(new WikiPageDummy("name", "content", null));
    page.getData().setAttribute(WikiPageProperty.DISABLE_TESTHISTORY, "true");
    final LinkedList<StringWriter> writers = new LinkedList<>();
    try (TestXmlFormatter formatter = new TestXmlFormatter(context,
        page.getSourcePage(), new WriterFactory() {
          @Override
          public Writer getWriter(FitNesseContext context, WikiPage page,
              TestSummary counts, long time) throws IOException {
            StringWriter writer = new StringWriter();
            writers.add(writer);
            return writer;
          }
        })) {
    }
    
    assertTrue(writers.isEmpty());
  }
}
