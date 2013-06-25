// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.components.ClassPathBuilder;
import fitnesse.slim.SlimClient;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class HtmlSlimResponder extends SlimResponder {

  protected SlimTestSystem getTestSystem() throws IOException {
    WikiPage page = getPage();
    SlimClient slimClient = new SlimClientBuilder(page.getData(), new ClassPathBuilder().getClasspath(page))
            .withFastTest(fastTest)
            .build();
    return new HtmlSlimTestSystem("slim", slimClient, this, new ExecutionLog(page, slimClient.getCommandRunner()));
  }
}
