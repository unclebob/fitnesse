// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.PageReferenceRenamingVisitor;
import fitnesse.wikitext.WidgetVisitor;

public class PageReferenceRenamer extends ReferenceRenamer {
  private WikiPage subjectPage;
  private String newName;

  public PageReferenceRenamer(WikiPage root) {
    super(root);
  }

  public void renameReferences(WikiPage subjectPage, String newName) throws Exception {
    this.subjectPage = subjectPage;
    this.newName = newName;
    renameReferences();
  }

  protected WidgetVisitor getVisitor() {
    return new PageReferenceRenamingVisitor(subjectPage, newName);
  }

}
