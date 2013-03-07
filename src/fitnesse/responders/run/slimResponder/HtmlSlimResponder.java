// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimTestSystem;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class HtmlSlimResponder extends SlimResponder {

  protected SlimTestSystem getTestSystem() {
    SlimTestSystem.SlimDescriptor descriptor = new SlimTestSystem.SlimDescriptor(getPage(), getContext().pageFactory, false);

    return new HtmlSlimTestSystem(getPage(), getDescriptor(), this);
  }
}
