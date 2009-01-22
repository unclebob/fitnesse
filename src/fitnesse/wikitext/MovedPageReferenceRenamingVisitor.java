// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.AliasLinkWidget;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class MovedPageReferenceRenamingVisitor implements WidgetVisitor {
  private WikiPage pageToBeMoved;
  private String newParentName;

  public MovedPageReferenceRenamingVisitor(WikiPage pageToBeMoved, String newParentName) {
    this.pageToBeMoved = pageToBeMoved;
    this.newParentName = newParentName;
  }

  public void visit(AliasLinkWidget widget) throws Exception {
  }

  public void visit(WikiWidget widget) throws Exception {
  }

  public void visit(WikiWordWidget widget) throws Exception {
    widget.renameMovedPageIfReferenced(pageToBeMoved, newParentName);
  }
}
