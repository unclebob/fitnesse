// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.ParentWidget;
import util.GracefulNamer;

public abstract class WikiWidget {
  protected ParentWidget parent = null;
  public static final String LINE_BREAK_PATTERN = "\n";

  protected WikiWidget(ParentWidget parent) {
    this.parent = parent;
    addToParent();
  }

  public ParentWidget getParent() {
    return parent;
  }

  public void setParent(ParentWidget parent) {
    this.parent = parent;
  }

  protected void addToParent() {
    if (this.parent != null)
      this.parent.addChild(this);
  }

  public abstract String render();

  public void acceptVisitor(WidgetVisitor visitor) {
    visitor.visit(this);
  }

  public WikiPage getWikiPage() {
    return parent.getWikiPage();
  }

  public String asWikiText() {
    return getClass().toString() + ".asWikiText()";
  }

  public boolean isRegracing() {
    return false;
  }

  public String regrace(String disgracefulName) {
    String newName = disgracefulName;
    //todo don't use the GracefulNamer for this.  It's only for java instance and variable names.  Write a different tool.
    if (isRegracing()) newName = GracefulNamer.regrace(disgracefulName);
    return newName;
  }

}

