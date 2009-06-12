// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetVisitor;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.AliasLinkWidget;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class MovedPageReferenceRenamer extends ReferenceRenamer implements WidgetVisitor {
  private WikiPage pageToBeMoved;
  private String newParentName;

  public MovedPageReferenceRenamer(WikiPage root, WikiPage pageToBeMoved, String newParentName) {
    super(root);
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
