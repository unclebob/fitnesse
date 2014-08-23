package fitnesse.wiki.fs;

import java.util.List;

import fitnesse.wiki.WikiPage;

public interface SubWikiPageFactory {

  List<WikiPage> getChildren(FileSystemPage fileSystemPage);

  WikiPage getChildPage(FileSystemPage fileSystemPage, String childName);
}
