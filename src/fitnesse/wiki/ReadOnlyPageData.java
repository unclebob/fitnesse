package fitnesse.wiki;

import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import java.util.List;

public interface ReadOnlyPageData {
    String getHtml();
    String getVariable(String name);
    Symbol getSyntaxTree();
    ParsingPage getParsingPage();
    String translateToHtml(Symbol syntaxTree);
    List<String> getClasspaths();
    List<String> getXrefPages();
    String getContent();
    String getAttribute(String attribute);
    boolean hasAttribute(String attribute);
    WikiPageProperties getProperties();
    WikiPage getWikiPage();
}
