package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.NoPruningStrategy;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wiki.WikiWordReference;
import fitnesse.wikitext.parser.Alias;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.WikiWord;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WhereUsedPageFinder implements TraversalListener<WikiPage>, PageFinder, SymbolTreeWalker {

  private final WikiPage subjectPage;
  private final TraversalListener<? super WikiPage> observer;
  private WikiPage currentPage;

  private List<WikiPage> hits = new ArrayList<>();

  public WhereUsedPageFinder(WikiPage subjectPage, TraversalListener<? super WikiPage> observer) {
    this.subjectPage = subjectPage;
    this.observer = observer;
  }

  @Override
  public void process(WikiPage currentPage) {
    this.currentPage = currentPage;

    checkSymbolicLinks();
    if (!hits.contains(currentPage)) {
      checkContent();
    }
  }

  private void checkSymbolicLinks() {
    WikiPageProperty suiteProperty = currentPage.getData().getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
    if (suiteProperty != null) {
      Set<String> links = suiteProperty.keySet();
      for (String link : links) {
        WikiPage linkTarget = currentPage.getChildPage(link);
        if (linkTarget instanceof SymbolicPage
          && ((SymbolicPage) linkTarget).getRealPage().equals(subjectPage)) {
          addHit();
          break;
        }
      }
    }
  }

  private void checkContent() {
    String content = currentPage.getData().getContent();
    Symbol syntaxTree = Parser.make(
      new ParsingPage(new WikiSourcePage(currentPage)),
      content,
      SymbolProvider.refactoringProvider)
      .parse();
    syntaxTree.walkPreOrder(this);
  }

  @Override
  public void search(WikiPage page) {
    hits.clear();
    page.getPageCrawler().traverse(this, new NoPruningStrategy());
  }

  @Override
  public boolean visit(Symbol node) {
    if (hits.contains(currentPage)) return true;
    if (node.isType(WikiWord.symbolType)) {
      WikiPage referencedPage = new WikiWordReference(currentPage, node.getContent()).getReferencedPage();
      if (referencedPage != null && referencedPage.equals(subjectPage)) {
        addHit();
      }
    }
    if (node.isType(Alias.symbolType)) {
      String linkText = node.childAt(1).childAt(0).getContent();
      if (linkText.contains("?")) {
        linkText = linkText.substring(0, linkText.indexOf('?'));
      }
      WikiPage referencedPage = new WikiWordReference(currentPage, linkText).getReferencedPage();
      if (referencedPage != null && referencedPage.equals(subjectPage)) {
        addHit();
      }
    }
    return true;
  }

  @Override
  public boolean visitChildren(Symbol node) {
    return true;
  }

  private void addHit() {
    hits.add(currentPage);
    observer.process(currentPage);
  }
}
