package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiWordReference;
import fitnesse.wikitext.parser.*;

import java.util.ArrayList;
import java.util.List;

public class WhereUsedPageFinder implements TraversalListener<WikiPage>, PageFinder, SymbolTreeWalker {
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

  public List<WikiPage> search(WikiPage page) {
    hits.clear();
    page.getPageCrawler().traverse(this);
    return hits;
  }

    public boolean visit(Symbol node) {
        if (!node.isType(WikiWord.symbolType)) return true;
        if (hits.contains(currentPage)) return true;
        try {
            WikiPage referencedPage = new WikiWordReference(currentPage, node.getContent()).getReferencedPage();
            if (referencedPage != null && referencedPage.equals(subjectPage)) {
              hits.add(currentPage);
              observer.process(currentPage);
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
