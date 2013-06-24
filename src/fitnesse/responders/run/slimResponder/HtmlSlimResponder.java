// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.slim.SlimClient;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class HtmlSlimResponder extends SlimResponder {

  protected SlimTestSystem getTestSystem() throws IOException {
    WikiPage page = getPage();
    SlimClientBuilder builder = new SlimClientBuilder(page.getData(), getDescriptor());
    builder.setFastTest(fastTest);
    SlimClient slimClient = builder.build();
    return new HtmlSlimTestSystem(slimClient, this, new ExecutionLog(page, slimClient.getTestRunner()));
  }
}
