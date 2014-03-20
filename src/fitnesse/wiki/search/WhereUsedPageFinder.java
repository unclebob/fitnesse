package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;
import fitnesse.wikitext.parser.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WhereUsedPageFinder implements TraversalListener<WikiPage>, PageFinder, SymbolTreeWalker {
  private static final Logger LOG = Logger.getLogger(WhereUsedPageFinder.class.getName());

  private WikiPage subjectPage;
  private TraversalListener<? super WikiPage> observer;
  private WikiPage currentPage;

  private List<WikiPage> hits = new ArrayList<WikiPage>();

  public WhereUsedPageFinder(WikiPage subjectPage, TraversalListener<? super WikiPage> observer) {
    this.subjectPage = subjectPage;
    this.observer = observer;
  }

  public void process(WikiPage currentPage) {
    this.currentPage = currentPage;
    String content = currentPage.getData().getContent();
    Symbol syntaxTree = Parser.make(
              new ParsingPage(new WikiSourcePage(currentPage), null),
              content,
              SymbolProvider.refactoringProvider)
              .parse();
      syntaxTree.walkPreOrder(this);
  }

  public void search(WikiPage page) {
    hits.clear();
    page.getPageCrawler().traverse(this);
  }

    public boolean visit(Symbol node) {
      if (hits.contains(currentPage)) return true;
      if (node.isType(WikiWord.symbolType)) {
        WikiPage referencedPage = new WikiWordReference(currentPage, node.getContent()).getReferencedPage();
        if (referencedPage != null && referencedPage.equals(subjectPage)) {
          hits.add(currentPage);
          observer.process(currentPage);
        }
      }
      if (node.isType(Alias.symbolType)) {
        String linkText = node.childAt(1).childAt(0).getContent();
        if (linkText.contains("?")) {
          linkText = linkText.substring(0, linkText.indexOf('?'));
        }
        WikiPage referencedPage = new WikiWordReference(currentPage, linkText).getReferencedPage();
        if (referencedPage != null && referencedPage.equals(subjectPage)) {
          hits.add(currentPage);
          observer.process(currentPage);
        }
      }
      return true;
    }

    public boolean visitChildren(Symbol node) {
      return true;
    }
}
