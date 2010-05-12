package fitnesse.components;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhereUsedPageFinder implements TraversalListener, SearchObserver, PageFinder, SymbolTreeWalker/*WidgetVisitor*/ {
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

  /*public void visit(WikiWidget widget) throws Exception {
  }

  public void visit(WikiWordWidget widget) throws Exception {
    if (hits.contains(currentPage))
      return;
    WikiPage referencedPage = widget.getReferencedPage();
    if (referencedPage != null && referencedPage.equals(subjectPage)) {
      hits.add(currentPage);
      observer.hit(currentPage);
    }
  }

  public void visit(AliasLinkWidget widget) throws Exception {
  }*/

  @SuppressWarnings("unchecked")
  public void processPage(WikiPage currentPage) throws Exception {
    this.currentPage = currentPage;
    String content = currentPage.getData().getContent();
    //WidgetBuilder referenceWidgetBuilder = new WidgetBuilder(new Class[]{PreProcessorLiteralWidget.class, WikiWordWidget.class, PreformattedWidget.class});
    //ParentWidget widgetRoot = new WidgetRoot(content, currentPage, referenceWidgetBuilder);
    //widgetRoot.acceptVisitor(this);
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
        if (!node.isType(SymbolType.WikiWord)) return true;
        if (hits.contains(currentPage)) return true;
        try {
            WikiPage referencedPage = getReferencedPage(node);
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

    private WikiPage getReferencedPage(Symbol wikiWord) throws Exception {
      String theWord = expandPrefix(wikiWord.getContent());
        WikiPage parentPage = currentPage.getParent();
      return parentPage.getPageCrawler().getPage(parentPage, PathParser.parse(theWord));
    }

    private String expandPrefix(String theWord) throws Exception {
      PageCrawler crawler = currentPage.getPageCrawler();
      if (theWord.charAt(0) == '^' || theWord.charAt(0) == '>') {
        String prefix = currentPage.getName();
        return String.format("%s.%s", prefix, theWord.substring(1));
      } else if (theWord.charAt(0) == '<') {
        String undecoratedPath = theWord.substring(1);
        String[] pathElements = undecoratedPath.split("\\.");
        String target = pathElements[0];
        //todo rcm, this loop is duplicated in PageCrawlerImpl.getSiblingPage
        for (WikiPage current = currentPage.getParent(); !crawler.isRoot(current); current = current.getParent()) {
          if (current.getName().equals(target)) {
            pathElements[0] = PathParser.render(crawler.getFullPath(current));
            return "." + StringUtil.join(Arrays.asList(pathElements), ".");
          }
        }
        return "." + undecoratedPath;
      }
      return theWord;
    }

    public boolean visitChildren(Symbol node) {
        return true;
    }
}
