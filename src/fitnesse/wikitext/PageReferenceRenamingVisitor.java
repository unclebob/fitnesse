// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.AliasLinkWidget;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class PageReferenceRenamingVisitor implements WidgetVisitor {
  private WikiPage pageToRename;
  private String newName;

  public PageReferenceRenamingVisitor(WikiPage pageToRename, String newName) {
    this.pageToRename = pageToRename;
    this.newName = newName;
  }

  public void visit(WikiWidget widget) throws Exception {
  }

  public void visit(WikiWordWidget widget) throws Exception {
    widget.renamePageIfReferenced(pageToRename, newName);
  }

  public void visit(AliasLinkWidget widget) throws Exception {
    widget.renamePageIfReferenced(pageToRename, newName);
  }
}
