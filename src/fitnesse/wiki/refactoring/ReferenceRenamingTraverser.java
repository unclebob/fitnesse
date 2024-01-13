package fitnesse.wiki.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.*;
import fitnesse.wikitext.MarkUpSystem;

import java.util.Optional;
import java.util.Set;

public class ReferenceRenamingTraverser implements TraversalListener<WikiPage> {

  public static void renameReferences(WikiPage root, ChangeReference changeReference) {
    root.getPageCrawler().traverse(new ReferenceRenamingTraverser(changeReference), new NoPruningStrategy());
  }

  private final ChangeReference changeReference;

  private ReferenceRenamingTraverser(ChangeReference changeReference) {
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

    String newContent = getUpdatedPageContent(currentPage);

    boolean pageHasChanged = !newContent.equals(content);
    if (pageHasChanged) {
      data.setContent(newContent);
    }
    return pageHasChanged;
  }

  private String getUpdatedPageContent(WikiPage currentPage) {
    return MarkUpSystem.make(currentPage.getData().getContent())
      .changeReferences(new WikiSourcePage(currentPage), reference -> changeReference.changeReference(currentPage, reference));
  }
}
