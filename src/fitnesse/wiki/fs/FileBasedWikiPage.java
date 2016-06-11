package fitnesse.wiki.fs;

import java.io.File;

import fitnesse.wiki.WikiPage;

public interface FileBasedWikiPage extends WikiPage {

  /**
   * Return the file name used to create sub-wiki's on.
   *
   * @return file
     */
  File getFileSystemPath();

}
