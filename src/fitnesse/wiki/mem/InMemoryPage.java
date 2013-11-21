// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.mem;

import java.util.Properties;

import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.fs.FileSystemPage;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.SimpleFileVersionsController;

// In memory page, used for testing and instant pages (like GitFileVersionController's RecentChanges page).
public class InMemoryPage {

  public static WikiPage makeRoot(String name) {
    return makeRoot(name, null);
  }

  public static WikiPage makeRoot(String name, Properties properties) {
    MemoryFileSystem fileSystem = new MemoryFileSystem();
    WikiPageFactory factory = new FileSystemPageFactory(fileSystem, new MemoryVersionsController(fileSystem), new SystemVariableSource(properties));
    FileSystemPage page = (FileSystemPage) factory.makeRootPage(null, name);
    page.autoCommit(true);
    return page;
  }

  public static WikiPage createChildPage(String name, FileSystemPage parent) {
    MemoryFileSystem fileSystem = new MemoryFileSystem();
    return new FileSystemPage(name, parent, fileSystem, new MemoryVersionsController(fileSystem));
  }
}
