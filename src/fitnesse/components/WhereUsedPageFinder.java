package fitnesse.components;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;
import fitnesse.wikitext.parser.*;

import java.util.ArrayList;
import java.util.List;

public class WhereUsedPageFinder implements TraversalListener, SearchObserver, PageFinder, SymbolTreeWalker {
  private WikiPage subjectPage;
  private SearchObserver observer;
  private WikiPage currentPage;

  private List<WikiPage> hits = new ArrayList<WikiPage>();

  public WhereUsedPageFinder(WikiPage subjectPage, SearchObserver observer) {
    this.subjectPage = subjectPage;
    this.observer = observer;
  }

  public void hit(WikiPage referencingPage) throws Exception {
  }

  public void processPage(WikiPage currentPage) throws Exception {
    this.currentPage = currentPage;
    String content = currentPage.getData().getContent();
      Symbol syntaxTree = Parser.make(
              new ParsingPage(new WikiSourcePage(currentPage)),
              content,
              SymbolProvider.refactoringProvider)
              .parse();
      syntaxTree.walkPreOrder(this);
  }

  public List<WikiPage> search(WikiPage page) throws Exception {
    hits.clear();
    subjectPage.getPageCrawler().traverse(page, this);
    return hits;
  }

    public boolean visit(Symbol node) {
        if (!node.isType(WikiWord.symbolType)) return true;
        if (hits.contains(currentPage)) return true;
        try {
            WikiPage referencedPage = new WikiWordReference(currentPage, node.getContent()).getReferencedPage();
            if (referencedPage != null && referencedPage.equals(subjectPage)) {
              hits.add(currentPage);
              observer.hit(currentPage);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean visitChildren(Symbol node) {
        return true;
    }
}
