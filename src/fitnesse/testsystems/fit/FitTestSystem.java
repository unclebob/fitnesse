// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.WikiPage;

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

  public void bye() throws IOException, InterruptedException {
    client.done();
    client.join();
  }

  @Override
  public void runTests(TestPage pageToTest) throws IOException, InterruptedException {
    String html = pageToTest.getDecoratedData().getHtml();
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
    String command = buildCommand(descriptor);
    Map<String, String> environmentVariables = createClasspathEnvironment(descriptor.getClassPath());
    client = new CommandRunningFitClient(this, command, context.port, environmentVariables, context.socketDealer, fastTest);
    setExecutionLog(new ExecutionLog(page, client.commandRunner));
    client.start();
  }
}