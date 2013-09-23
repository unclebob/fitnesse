// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;
import fitnesse.wikitext.parser.Alias;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.WikiWord;

public class MovedPageReferenceRenamer extends ReferenceRenamer {
  private static final Logger LOG = Logger.getLogger(MovedPageReferenceRenamer.class.getName());

  private WikiPage pageToBeMoved;
  private String newParentName;

  public MovedPageReferenceRenamer(WikiPage root, WikiPage pageToBeMoved, String newParentName) {
    super(root);
    this.pageToBeMoved = pageToBeMoved;
    this.newParentName = newParentName;
  }

    public boolean visit(Symbol node) {
        try {
            if (node.isType(WikiWord.symbolType)) {
                new WikiWordReference(currentPage, node.getContent()).wikiWordRenameMovedPageIfReferenced(node, pageToBeMoved, newParentName);
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
