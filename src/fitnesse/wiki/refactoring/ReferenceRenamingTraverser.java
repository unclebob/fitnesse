package fitnesse.wiki.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.WikiTranslator;

class ReferenceRenamingTraverser implements TraversalListener<WikiPage> {
  private final SymbolTreeWalker walker;
  private WikiPage currentPage;

  ReferenceRenamingTraverser(SymbolTreeWalker walker) {
    this.walker = walker;
  }

  @Override
  public void process(WikiPage currentPage) {
    this.currentPage = currentPage;
    PageData data = currentPage.getData();
    boolean pageHasChanged = updatePageContent(data);

    if (pageHasChanged) {
      currentPage.commit(data);
    }
  }

  private boolean updatePageContent(PageData data) {
    String content = data.getContent();

    String newContent = getUpdatedPageContent(content);

    boolean pageHasChanged = !newContent.equals(content);
    if (pageHasChanged) {
      data.setContent(newContent);
    }
    return pageHasChanged;
  }

  private String getUpdatedPageContent(String content) {
    Symbol syntaxTree = Parser.make(
      new ParsingPage(new WikiSourcePage(currentPage)),
      content,
      SymbolProvider.refactoringProvider)
      .parse();
    syntaxTree.walkPreOrder(walker);
    return new WikiTranslator(new WikiSourcePage(currentPage)).translateTree(syntaxTree);
  }

  WikiPage currentPage() {
    return currentPage;
  }
}
