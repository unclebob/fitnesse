// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.*;
import fitnesse.wikitext.translator.WikiTranslator;
import util.StringUtil;

import java.util.Arrays;

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

      Symbol syntaxTree = Parser.make(new ParsingPage(new WikiSourcePage(currentPage)), content).parse();
      this.currentPage = currentPage;
      syntaxTree.walkPreOrder(this);
      String newContent = new WikiTranslator(new WikiSourcePage(currentPage)).translateTree(syntaxTree);

    boolean pageHasChanged = !newContent.equals(content);
    if (pageHasChanged) {
      data.setContent(newContent);
      currentPage.commit(data);
    }
  }

    protected String getQualifiedWikiWord(String wikiWordText) throws Exception {
      String pathName = expandPrefix(wikiWordText);
      WikiPagePath expandedPath = PathParser.parse(pathName);
      if (expandedPath == null)
        return wikiWordText;
      WikiPagePath fullPath = currentPage.getParent().getPageCrawler().getFullPathOfChild(currentPage.getParent(), expandedPath);
      return "." + PathParser.render(fullPath); //todo rcm 2/6/05 put that '.' into pathParser.  Perhaps WikiPagePath.setAbsolute()
    }

    private String expandPrefix(String theWord) throws Exception {
      WikiPage wikiPage = currentPage;
      return expandPrefix(wikiPage, theWord);
    }

    private String expandPrefix(WikiPage wikiPage, String theWord) throws Exception {
      PageCrawler crawler = wikiPage.getPageCrawler();
      if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
        String prefix = wikiPage.getName();
        return String.format("%s.%s", prefix, theWord.substring(1));
      } else if (theWord.charAt(0) == '<') {
        String undecoratedPath = theWord.substring(1);
        String[] pathElements = undecoratedPath.split("\\.");
        String target = pathElements[0];
        //todo rcm, this loop is duplicated in PageCrawlerImpl.getSiblingPage
        for (WikiPage current = wikiPage.getParent(); !crawler.isRoot(current); current = current.getParent()) {
          if (current.getName().equals(target)) {
            pathElements[0] = PathParser.render(crawler.getFullPath(current));
            return "." + StringUtil.join(Arrays.asList(pathElements), ".");
          }
        }
        return "." + undecoratedPath;
      }
      return theWord;
    }
    
    protected boolean refersTo(String qualifiedReference, String qualifiedTarget) {
        return qualifiedReference.equals(qualifiedTarget) || qualifiedReference.startsWith(qualifiedTarget + ".");
    }
}
