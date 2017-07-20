package fitnesse.wiki.fs;

import java.util.List;

import fitnesse.wiki.WikiPage;

public interface SubWikiPageFactory {

  List<WikiPage> getChildren(FileBasedWikiPage wikiPage);

  WikiPage getChildPage(FileBasedWikiPage wikiPage, String childName);
}
