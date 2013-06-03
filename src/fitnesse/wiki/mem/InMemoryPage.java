// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.mem;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.fs.FileSystemPage;
import fitnesse.wiki.fs.FileSystemPageFactory;

// In memory page, used for testing and instant pages (like GitFileVersionController's RecentChanges page).
public class InMemoryPage {

  public static WikiPage makeRoot(String name) {
    MemoryFileSystem fileSystem = new MemoryFileSystem();
    WikiPageFactory factory = new FileSystemPageFactory(fileSystem, new MemoryVersionsController(fileSystem));
    return factory.makeRootPage(null, name);
  }

  public static WikiPage createChildPage(String name, FileSystemPage parent) {
    MemoryFileSystem fileSystem = new MemoryFileSystem();
    return new FileSystemPage(name, parent, fileSystem, new MemoryVersionsController(fileSystem));
  }
}
