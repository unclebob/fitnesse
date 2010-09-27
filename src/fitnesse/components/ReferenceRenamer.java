// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;

public abstract class ReferenceRenamer implements TraversalListener, SymbolTreeWalker {
  protected WikiPage root;
    protected WikiPage currentPage;

  public ReferenceRenamer(WikiPage root) {
    this.root = root;
  }

  public void renameReferences() throws Exception {
    root.getPageCrawler().traverse(root, this);
  }

  public void processPage(WikiPage currentPage) throws Exception {
    PageData data = currentPage.getData();
    String content = data.getContent();

      Symbol syntaxTree = Parser.make(
              new ParsingPage(new WikiSourcePage(currentPage)),
              content,
              SymbolProvider.refactoringProvider)
              .parse();
      this.currentPage = currentPage;
      syntaxTree.walkPreOrder(this);
      String newContent = new WikiTranslator(new WikiSourcePage(currentPage)).translateTree(syntaxTree);

    boolean pageHasChanged = !newContent.equals(content);
    if (pageHasChanged) {
      data.setContent(newContent);
      currentPage.commit(data);
    }
  }
}
