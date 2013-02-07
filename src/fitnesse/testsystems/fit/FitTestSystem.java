// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.Map;

public class FitTestSystem extends TestSystem {
  protected static final String EMPTY_PAGE_CONTENT = "OH NO! This page is empty!";

  private CommandRunningFitClient client;
  private FitNesseContext context;
  private final Descriptor descriptor;

  public FitTestSystem(FitNesseContext context, WikiPage page, Descriptor descriptor,
                       TestSystemListener listener) {
    super(page, listener);
    this.descriptor = descriptor;
    this.context = context;
  }

  protected ExecutionLog createExecutionLog() {
    String command = buildCommand(descriptor);
    Map<String, String> environmentVariables = createClasspathEnvironment(descriptor.getClassPath());
    client = new CommandRunningFitClient(this, command, context.port, environmentVariables, context.socketDealer, fastTest);
    return new ExecutionLog(page, client.commandRunner);
  }


  public void bye() throws IOException, InterruptedException {
    client.done();
    client.join();
  }

  @Override
  public void runTests(ReadOnlyPageData pageData) throws IOException, InterruptedException {
    String html = pageData.getHtml();
    if (html.length() == 0)
      client.send(EMPTY_PAGE_CONTENT);
    else
      client.send(html);
  }

  public boolean isSuccessfullyStarted() {
    return client.isSuccessfullyStarted();
  }

  public void kill() {
    client.kill();
  }

  public void start() {
    client.start();
  }
}