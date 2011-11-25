// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;

import java.util.List;

public class TextIgnoringWidgetRoot extends WidgetRoot {
  //Refactored for isGathering parameter.
  public TextIgnoringWidgetRoot(String value, WikiPage page, WidgetBuilder builder) {
    super(value, page, builder, /*isGatheringInfo=*/ true);
  }

  //Parent Literals: T'I'W'Root ctor with parent's literals
  public TextIgnoringWidgetRoot(String value, WikiPage page, List<String> literals, WidgetBuilder builder) {
    super(null, page, builder, /*isGatheringInfo=*/ true);
    if (literals != null) this.setLiterals(literals);
    this.buildWidgets(value);
  }


  public void addChildWidgets(String value) {
    getBuilder().addChildWidgets(value, this, false);
  }
}

