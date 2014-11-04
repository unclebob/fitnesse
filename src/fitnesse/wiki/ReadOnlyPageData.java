package fitnesse.wiki;

import fitnesse.wikitext.parser.ParsedPage;
import java.util.List;

public interface ReadOnlyPageData {
    String getContent();
    String getAttribute(String attribute);
    boolean hasAttribute(String attribute);
    WikiPageProperties getProperties();
}
