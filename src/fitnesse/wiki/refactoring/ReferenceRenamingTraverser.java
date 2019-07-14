package fitnesse.wiki.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolTreeWalker;
import fitnesse.wikitext.parser.WikiTranslator;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

class ReferenceRenamingTraverser implements TraversalListener<WikiPage> {
  private final SymbolTreeWalker walker;
  private final Function<String, Optional<String>> symbolicLinkProcessor;
  private WikiPage currentPage;

  ReferenceRenamingTraverser(SymbolTreeWalker walker, Function<String, Optional<String>> symbolicLinkProcessor) {
    this.walker = walker;
    this.symbolicLinkProcessor = symbolicLinkProcessor;
  }

  @Override
  public void process(WikiPage currentPage) {
    this.currentPage = currentPage;
    PageData data = currentPage.getData();
    boolean pageHasChanged = checkSymbolicLinks(data);
    pageHasChanged |= updatePageContent(data);

    if (pageHasChanged) {
      currentPage.commit(data);
    }
  }

  private boolean checkSymbolicLinks(PageData data) {
    boolean pageHasChanged = false;
    WikiPageProperty suiteProperty = data.getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
    if (suiteProperty != null) {
      Set<String> links = suiteProperty.keySet();
      for (String link : links) {
        String linkTarget = suiteProperty.get(link);
        Optional<String> renamedLinkTarget = symbolicLinkProcessor.apply(linkTarget);
        if (renamedLinkTarget.isPresent()) {
          suiteProperty.set(link, renamedLinkTarget.get());
          pageHasChanged = true;
        }
      }
    }
    return pageHasChanged;
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
