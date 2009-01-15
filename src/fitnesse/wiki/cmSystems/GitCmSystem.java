package fitnesse.wiki.cmSystems;

import java.io.IOException;

/**
 * The three functions: cmUpdate, cmEdit, and cmDelete are called if:
 *   1. The name of this class is in the CM_SYSTEM variable accessible from the page and
 *   2. that page is being created, modified, or deleted.
 *
 * cmEdit(file, payload) is called just before file is about to be written
 * cmUpdate(file, payload) is called just after file has been written
 * cmDelete(directory, payload) is called just after the directory defining a page has been deleted.
 *
 * Remember that each page is defined by a directory that bears it's name, and two files that contain it's content.
 * The first file is content.txt which holds the wiki text.  The second is properties.xml which holds all the metatdata
 * for the page.  The file operations cmEdit and cmUpdate are called for each file.  The file argument is the absolute
 * path of the file.  The cmDelete function is called with the absolute path of the directory that holds the content of
 * the page, and all the sub-pages.
 *
 * The 'payload' is there just in case you need it.  It's the complete definition of the CM_SYSTEM variable.  You can
 * put whatever you like in this variable, so long as the fully qualified name of this class (or whatever class you
 * create for yourself) comes first.  Use a space to separate the classname from whatever else you want.  What would
 * you put in this payload?  You might put your username and password for the CM system...  Or you might put the path
 * of the CM root.  Anything you need to make the CM system work...
 *
 * So given: !define CM_SYSTEM {fitnesse.wiki.cmSystems.MyCmSystem unclebob/password /cm/myResponsitory}
 * Then the payload would be: fitnesse.wiki.cmSystems.MyCmSystem unclebob/password /cm/myResponsitory
 *
 * Oh, by the way, if there is no !define for CM_SYSTEM, fitnesse is happy to use the CM_SYSTEM environment variable
 * or system property in its place (as for all variables).
 */
public class GitCmSystem {
  public static void cmUpdate(String file, String payload) throws IOException {
    Runtime.getRuntime().exec("/usr/local/bin/git add " + file);
  }

  public static void cmEdit(String file, String payload) {
   //git doesn't need this.
  }

  public static void cmDelete(String file, String payload) throws IOException {
    Runtime.getRuntime().exec("/usr/local/bin/git rm -rf --cached " + file);
  }
}
