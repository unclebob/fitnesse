package fitnesse.wiki;

import fitnesse.wikitext.parser.ParsedPage;
import java.util.List;

public interface ReadOnlyPageData {
    String getHtml();
    String getVariable(String name);
    ParsedPage getParsedPage();
    List<String> getClasspaths();
    List<String> getXrefPages();
    String getContent();
    String getAttribute(String attribute);
    boolean hasAttribute(String attribute);
    WikiPageProperties getProperties();
    WikiPage getWikiPage();
}
