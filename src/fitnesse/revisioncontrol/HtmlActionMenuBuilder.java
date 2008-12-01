package fitnesse.revisioncontrol;

import fitnesse.html.HtmlTag;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class HtmlActionMenuBuilder {

  public static void addRevisionControlActions(HtmlTag tag, String pageName, PageData pageData) throws Exception {
    if (pageData.hasAttribute("Edit") || pageData.hasAttribute("WikiImport")) {
      final WikiPage wikiPage = pageData.getWikiPage();
      if (shouldDisplayRevisionControlActions(wikiPage)) {
        tag.add(makeRevisionControlActionMenuHeader());
        final State state = ((RevisionControllable) wikiPage).checkState();
        final RevisionControlOperation[] operations = state.operations();
        for (final RevisionControlOperation operation : operations)
          tag.add(operation.makeActionLink(pageName));
      }
    }
  }

  private static boolean shouldDisplayRevisionControlActions(WikiPage wikiPage) throws Exception {
    if (wikiPage instanceof FileSystemPage)
      return ((RevisionControllable) wikiPage).isExternallyRevisionControlled();
    return false;
  }

  private static HtmlTag makeRevisionControlActionMenuHeader() {
    final HtmlTag navBreak = new HtmlTag("div");
    navBreak.addAttribute("class", "main");
    navBreak.add("Revision Control");
    return navBreak;
  }

}
