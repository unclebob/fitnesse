// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;
import fitnesse.wikitext.parser.*;

public class PageReferenceRenamer extends ReferenceRenamer {
  private static final Logger LOG = Logger.getLogger(PageReferenceRenamer.class.getName());

  private WikiPage subjectPage;
  private String newName;

  public PageReferenceRenamer(WikiPage root, WikiPage subjectPage, String newName) {
    super(root);
    this.subjectPage = subjectPage;
    this.newName = newName;
  }

    public boolean visit(Symbol node) {
        try {
            if (node.isType(WikiWord.symbolType)) {
                new WikiWordReference(currentPage, node.getContent()).wikiWordRenamePageIfReferenced(node, subjectPage, newName);
            }
            else if (node.isType(Alias.symbolType)) {
                String aliasReference = node.childAt(1).childAt(0).getContent();
                if (PathParser.isWikiPath(aliasReference)) {
                   new WikiWordReference(currentPage, aliasReference).wikiWordRenamePageIfReferenced(node.childAt(1).childAt(0), subjectPage, newName);
                }
            }
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "Unable to rename moved page references", e);
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean visitChildren(Symbol node) {
        return !node.isType(Alias.symbolType);
    }
}
