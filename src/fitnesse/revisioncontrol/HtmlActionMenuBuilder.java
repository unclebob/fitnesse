package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.STATE;
import fitnesse.html.HtmlTag;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class HtmlActionMenuBuilder {

    public static void addRevisionControlActions(HtmlTag tag, String pageName, PageData pageData) throws Exception {
        if (pageData.hasAttribute("Edit") || pageData.hasAttribute("WikiImport")) {
            WikiPage wikiPage = pageData.getWikiPage();
            if (shouldDisplayRevisionControlActions(wikiPage)) {
                tag.add(makeRevisionControlActionMenuHeader());
                State state = ((FileSystemPage) wikiPage).execute(STATE);
                RevisionControlOperation[] operations = state.operations();
                for (RevisionControlOperation operation : operations) {
                    tag.add(operation.makeActionLink(pageName));
                }
            }
        }
    }

    private static boolean shouldDisplayRevisionControlActions(WikiPage wikiPage) throws Exception {
        if (wikiPage instanceof FileSystemPage) {
            return ((FileSystemPage) wikiPage).isRevisionControlled();
        }
        return false;
    }

    private static HtmlTag makeRevisionControlActionMenuHeader() {
        HtmlTag navBreak = new HtmlTag("div");
        navBreak.addAttribute("class", "main");
        navBreak.add("Revision Control");
        return navBreak;
    }

}
