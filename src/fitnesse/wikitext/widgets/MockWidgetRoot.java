// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.PagePointer;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.WidgetBuilder;

public class MockWidgetRoot extends WidgetRoot {
  public MockWidgetRoot() throws Exception {
    super(null, new PagePointer(new WikiPageDummy("RooT"), new WikiPagePath()), WidgetBuilder.htmlWidgetBuilder);
  }

  protected void buildWidgets(String value) throws Exception {
  }
}
