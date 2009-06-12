// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.AliasLinkWidget;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class PageReferenceRenamer extends ReferenceRenamer {
  private WikiPage subjectPage;
  private String newName;

  public PageReferenceRenamer(WikiPage root, WikiPage subjectPage, String newName) {
    super(root);
    this.subjectPage = subjectPage;
    this.newName = newName;
  }

  public void visit(WikiWidget widget) throws Exception {
  }

  public void visit(WikiWordWidget widget) throws Exception {
    widget.renamePageIfReferenced(subjectPage, newName);
  }

  public void visit(AliasLinkWidget widget) throws Exception {
    widget.renamePageIfReferenced(subjectPage, newName);
  }
}
