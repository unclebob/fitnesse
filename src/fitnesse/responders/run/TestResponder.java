// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.http.Response;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.run.formatters.*;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

public class TestResponder extends ChunkingResponder implements SecureResponder {
  private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
  protected PageData data;
  protected CompositeFormatter formatters;
  private volatile boolean isClosed = false;

  private boolean fastTest = false;
  private boolean remoteDebug = false;
  protected TestSystem testSystem;

  public TestResponder() {
    super();
    formatters = new CompositeFormatter();
  }

  protected void doSending() throws Exception {
    checkArguments();
    data = page.getData();

    createFormatterAndWriteHead();
    sendPreTestNotification();
    performExecution();

    int exitCode = formatters.getErrorCount();
    closeHtmlResponse(exitCode);
  }

  protected void checkArguments() {
    fastTest |= request.hasInput("debug");
    remoteDebug |= request.hasInput("remote_debug");
  }

  protected void createFormatterAndWriteHead() throws Exception {
    if (response.isXmlFormat())
      addXmlFormatter();
    else if (response.isTextFormat())
      addTextFormatter();
    else if (response.isJavaFormat())
      addJavaFormatter();
    else
      addHtmlFormatter();
    if (!request.hasInput("nohistory"))
      addTestHistoryFormatter();
	addTestInProgressFormatter();
    formatters.writeHead(getTitle());
  }

  String getTitle() {
    return "Test Results";
  }

  void addXmlFormatter() throws Exception {
    XmlFormatter.WriterFactory writerSource = new XmlFormatter.WriterFactory() {
      public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) {
        return makeResponseWriter();
      }
    };
    formatters.add(new XmlFormatter(context, page, writerSource));
  }

  void addTextFormatter() {
    formatters.add(new TestTextFormatter(response));
  }
  void addJavaFormatter() throws Exception{
    formatters.add(JavaFormatter.getInstance(new WikiPagePath(page).toString()));
  }
  protected Writer makeResponseWriter() {
    return new Writer() {
      public void write(char[] cbuf, int off, int len) {
        String fragment = new String(cbuf, off, len);
        try {
          response.add(fragment.getBytes());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public void flush() throws IOException {
      }

      public void close() throws IOException {
      }
    };
  }


  void addHtmlFormatter() throws Exception {
    BaseFormatter formatter = new TestHtmlFormatter(context, page, context.htmlPageFactory) {
      @Override
      protected void writeData(String output) throws Exception {
        addToResponse(output);
      }
    };
    formatters.add(formatter);
  }

  protected void addTestHistoryFormatter() throws Exception {
    HistoryWriterFactory writerFactory = new HistoryWriterFactory();
    formatters.add(new PageHistoryFormatter(context, page, writerFactory));
  }
  
  protected void addTestInProgressFormatter() throws Exception {
    formatters.add(new PageInProgressFormatter(page));
  }

  protected void sendPreTestNotification() throws Exception {
    for (TestEventListener eventListener : eventListeners) {
      eventListener.notifyPreTest(this, data);
    }
  }

  protected void performExecution() throws Exception {
    List<WikiPage> test2run = new SuiteContentsFinder(page, null, root).makePageListForSingleTest();

    MultipleTestsRunner runner = new MultipleTestsRunner(test2run, context, page, formatters);
    runner.setFastTest(fastTest);
    runner.setDebug(isRemoteDebug());

    if (isEmpty(page))
      formatters.addMessageForBlankHtml();
    runner.executeTestPages();
  }

  private boolean isEmpty(WikiPage page) throws Exception {
    return page.getData().getContent().length() == 0;
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }


  public static void registerListener(TestEventListener listener) {
    eventListeners.add(listener);
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public boolean isFastTest() {
    return fastTest;
  }

  public void addToResponse(byte[] output) throws Exception {
    if (!isClosed()) {
      response.add(output);
    }
  }

  public void addToResponse(String output) throws Exception {
    if (!isClosed()) {
      response.add(output);
    }
  }

  synchronized boolean isClosed() {
    return isClosed;
  }

  synchronized void setClosed() {
    isClosed = true;
  }

  void closeHtmlResponse(int exitCode) throws Exception {
    if (!isClosed()) {
      setClosed();
      response.closeChunks();
      response.addTrailingHeader("Exit-Code", String.valueOf(exitCode));
      response.closeTrailer();
      response.close();
    }
  }

  void closeHtmlResponse() throws Exception {
    if (!isClosed()) {
      setClosed();
      response.closeChunks();
      response.close();
    }
  }

  boolean isRemoteDebug() {
    return remoteDebug;
  }

  public Response getResponse() {
    return response;
  }

  public static class HistoryWriterFactory implements XmlFormatter.WriterFactory {
    public Writer getWriter(FitNesseContext context, WikiPage page, TestSummary counts, long time) throws Exception {
      File resultPath = new File(PageHistory.makePageHistoryFileName(context, page, counts, time));
      File resultDirectory = new File(resultPath.getParent());
      resultDirectory.mkdirs();
      File resultFile = new File(resultDirectory, resultPath.getName());
      return new FileWriter(resultFile);
    }
  }
}
