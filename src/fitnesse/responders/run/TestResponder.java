// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.responders.ChunkingResponder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.LinkedList;
import java.util.List;

public class TestResponder extends ChunkingResponder implements SecureResponder {
  private static LinkedList<TestEventListener> eventListeners = new LinkedList<TestEventListener>();
  protected PageData data;
  protected BaseFormatter formatter;
  private boolean isClosed = false;

  private boolean fastTest = false;
  protected TestSystem testSystem;

  protected void doSending() throws Exception {
    fastTest |= request.hasInput("debug");
    data = page.getData();

    createFormatterAndWriteHead();

    sendPreTestNotification();

    performExecution();

    formatter.allTestingComplete();
  }

  protected void createFormatterAndWriteHead() throws Exception {
    if (response.isXmlFormat()) {
      formatter = createXmlFormatter();
    } else {
      formatter = createHtmlFormatter();
    }

    formatter.writeHead(getTitle());
  }

  String getTitle() {
    return "Test Results";
  }

  BaseFormatter createXmlFormatter() throws Exception {
    BaseFormatter formatter = new XmlFormatter(context, page) {
      @Override
      protected void close() throws Exception {
        closeHtmlResponse();
      }

      @Override
      protected void writeData(byte[] byteArray) throws Exception {
        response.add(byteArray);
      }
    };
    return formatter;
  }


  BaseFormatter createHtmlFormatter() throws Exception {
    BaseFormatter formatter = new TestHtmlFormatter(context, page, context.htmlPageFactory) {
      @Override
      protected void writeData(String output) throws Exception {
        addToResponse(output);
      }

      @Override
      protected void close() throws Exception {
        closeHtmlResponse(exitCode());
      }
    };
    return formatter;
  }

  protected void sendPreTestNotification() throws Exception {
    for (TestEventListener eventListener : eventListeners) {
      eventListener.notifyPreTest(this, data);
    }
  }

  protected void performExecution() throws Exception {
    List<WikiPage> test2run = new SuiteContentsFinder(page, root, null).makePageListForSingleTest();

    MultipleTestsRunner runner = new MultipleTestsRunner(test2run, context, page, formatter);
    runner.setFastTest(fastTest);

    if (page.getData().getContent().length() == 0 && formatter instanceof TestHtmlFormatter) {
      ((TestHtmlFormatter) formatter).addMessageForBlankHtml();
    }

    runner.executeTestPages();
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
}
