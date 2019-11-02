// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;
import fitnesse.wikitext.parser.Alias;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.WikiWord;

import java.util.Optional;

public class PageReferenceRenamer extends ReferenceRenamer {
  private WikiPage subjectPage;
  private String newName;

  public PageReferenceRenamer(WikiPage root, WikiPage subjectPage, String newName) {
    super(root);
    this.subjectPage = subjectPage;
    this.newName = newName;
  }

  @Override
  public boolean visit(Symbol node) {
    if (node.isType(WikiWord.symbolType)) {
      new WikiWordReference(currentPage(), node.getContent()).wikiWordRenamePageIfReferenced(node, subjectPage, newName);
    } else if (node.isType(Alias.symbolType)) {
      Symbol wikiWord = node.childAt(1).childAt(0);
      String aliasReference = wikiWord.getContent();
      if (PathParser.isWikiPath(aliasReference)) {
        new WikiWordReference(currentPage(), aliasReference).wikiWordRenamePageIfReferenced(wikiWord, subjectPage, newName);
      }
    }
    return true;
  }

  @Override
  public boolean visitChildren(Symbol node) {
    return !node.isType(Alias.symbolType);
  }

  @Override
  Optional<String> renameSymbolicLinkIfNeeded(String linkTarget) {
    return new WikiWordReference(currentPage(), linkTarget).getRenamedContent(linkTarget, subjectPage, newName);
  }
}
