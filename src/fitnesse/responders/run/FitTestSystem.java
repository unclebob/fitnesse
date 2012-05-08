// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.components.CommandRunningFitClient;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.Map;

public class FitTestSystem extends TestSystem {
 private CommandRunningFitClient client;
 private FitNesseContext context;

 public FitTestSystem(FitNesseContext context, WikiPage page, TestSystemListener listener) {
   super(page, listener);
   this.context = context;
 }

 protected ExecutionLog createExecutionLog(String classPath, Descriptor descriptor) {
   String command = buildCommand(descriptor, classPath);
   Map<String, String> environmentVariables = createClasspathEnvironment(classPath);
   client = new CommandRunningFitClient(this, command, context.port, environmentVariables, context.socketDealer, fastTest);
   return new ExecutionLog(page, client.commandRunner, context.pageFactory);
 }


  public void bye() throws IOException, InterruptedException {
   client.done();
   client.join();
 }

 public String runTestsAndGenerateHtml(ReadOnlyPageData pageData) throws IOException, InterruptedException {
   String html = pageData.getHtml();
   if (html.length() == 0)
     client.send(emptyPageContent);
   else
     client.send(html);
   return html;
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