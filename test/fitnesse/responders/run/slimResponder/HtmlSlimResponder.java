// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.testsystems.slim.SlimCommandRunningClient;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.InProcessSlimClientBuilder;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.SlimTestSystem;

import java.io.IOException;

public class HtmlSlimResponder extends SlimResponder {

  protected SlimTestSystem getTestSystem() throws IOException {

    SlimCommandRunningClient slimClient;
    if (fastTest) {
      slimClient = new InProcessSlimClientBuilder(getDescriptor()).build();
    } else {
      slimClient = new SlimClientBuilder(getDescriptor()).build();
    }
    return new HtmlSlimTestSystem("slim", slimClient, this);
  }

}
