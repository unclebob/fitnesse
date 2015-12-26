// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.SlimClient;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.InProcessSlimClientBuilder;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

import java.io.IOException;

public class HtmlSlimResponder extends SlimResponder {
  private final CustomComparatorRegistry customComparatorRegistry;

  public HtmlSlimResponder(CustomComparatorRegistry customComparatorRegistry) {
    this.customComparatorRegistry = customComparatorRegistry;
  }

  @Override
  protected SlimTestSystem getTestSystem() throws IOException {

    SlimClient slimClient;
    if (fastTest) {
      slimClient = new InProcessSlimClientBuilder(getDescriptor()).build();
    } else {
      slimClient = new SlimClientBuilder(getDescriptor()).build();
    }
    SlimTestSystem testSystem = new HtmlSlimTestSystem("slim", slimClient,
            new SlimTableFactory(), customComparatorRegistry);
    testSystem.addTestSystemListener(this);
    return testSystem;
  }

}
