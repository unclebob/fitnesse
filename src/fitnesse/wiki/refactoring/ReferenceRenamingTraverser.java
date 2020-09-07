package fitnesse.wiki.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SyntaxTreeV2;

import java.util.Optional;
import java.util.Set;

public class ReferenceRenamingTraverser implements TraversalListener<WikiPage> {
  private final ChangeReference changeReference;

  public ReferenceRenamingTraverser(ChangeReference changeReference) {
    this.changeReference = changeReference;
  }

  @Override
  public void process(WikiPage currentPage) {
    PageData data = currentPage.getData();
    boolean pageHasChanged = checkSymbolicLinks(currentPage, data);
    pageHasChanged |= updatePageContent(currentPage, data);

    if (pageHasChanged) {
      currentPage.commit(data);
    }
  }

  private boolean checkSymbolicLinks(WikiPage currentPage, PageData data) {
    boolean pageHasChanged = false;
    WikiPageProperty suiteProperty = data.getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
    if (suiteProperty != null) {
      Set<String> links = suiteProperty.keySet();
      for (String link : links) {
        String linkTarget = suiteProperty.get(link);
        Optional<String> renamedLinkTarget = changeReference.changeReference(currentPage, linkTarget);
        if (renamedLinkTarget.isPresent()) {
          suiteProperty.set(link, renamedLinkTarget.get());
          pageHasChanged = true;
        }
      }
    }
    return pageHasChanged;
  }


  private boolean updatePageContent(WikiPage currentPage, PageData data) {
    String content = data.getContent();

    String newContent = getUpdatedPageContent(currentPage, content);

    boolean pageHasChanged = !newContent.equals(content);
    if (pageHasChanged) {
      data.setContent(newContent);
    }
    return pageHasChanged;
  }

  private String getUpdatedPageContent(WikiPage currentPage, String content) {
    SyntaxTreeV2 syntaxTree = new SyntaxTreeV2(SymbolProvider.refactoringProvider);
    syntaxTree.parse(content, new ParsingPage(new WikiSourcePage(currentPage)));
    syntaxTree.findReferences(reference -> changeReference.changeReference(currentPage, reference));
    return syntaxTree.translateToMarkUp();
  }
}
