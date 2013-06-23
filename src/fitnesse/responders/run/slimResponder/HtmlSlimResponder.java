// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.slim.SlimClient;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class HtmlSlimResponder extends SlimResponder {

  protected SlimTestSystem getTestSystem() throws IOException {
    WikiPage page = getPage();
    SlimClientBuilder builder = new SlimClientBuilder(page, getDescriptor());
    builder.setFastTest(fastTest);
    builder.start();
    SlimClient slimClient = builder.getSlimClient();
    return new HtmlSlimTestSystem(getPage(), slimClient, this);
  }
}
