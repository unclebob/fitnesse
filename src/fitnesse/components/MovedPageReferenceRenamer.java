// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.Alias;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;

public class MovedPageReferenceRenamer extends ReferenceRenamer /* implements WidgetVisitor*/ {
  private WikiPage pageToBeMoved;
  private String newParentName;

  public MovedPageReferenceRenamer(WikiPage root, WikiPage pageToBeMoved, String newParentName) {
    super(root);
    this.pageToBeMoved = pageToBeMoved;
    this.newParentName = newParentName;
  }

    public boolean visit(Symbol node) {
        try {
            if (node.isType(SymbolType.WikiWord)) {
                wikiWordRenameMovedPageIfReferenced(node, pageToBeMoved, newParentName);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean visitChildren(Symbol node) {
        return !node.isType(Alias.symbolType);
    }

    private void wikiWordRenameMovedPageIfReferenced(Symbol wikiWord, WikiPage pageToBeMoved, String newParentName) throws Exception {
      WikiPagePath pathOfPageToBeMoved = pageToBeMoved.getPageCrawler().getFullPath(pageToBeMoved);
      pathOfPageToBeMoved.makeAbsolute();
      String QualifiedNameOfPageToBeMoved = PathParser.render(pathOfPageToBeMoved);
      String reference = getQualifiedWikiWord(wikiWord.getContent());
      if (refersTo(reference, QualifiedNameOfPageToBeMoved)) {
        String referenceTail = reference.substring(QualifiedNameOfPageToBeMoved.length());
        String childPortionOfReference = pageToBeMoved.getName();
        if (referenceTail.length() > 0)
          childPortionOfReference += referenceTail;
        String newQualifiedName;
        if ("".equals(newParentName))
          newQualifiedName = "." + childPortionOfReference;
        else
          newQualifiedName = "." + newParentName + "." + childPortionOfReference;

        wikiWord.setContent(newQualifiedName);
      }
    }
}
