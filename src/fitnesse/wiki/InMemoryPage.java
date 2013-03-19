// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wiki.storage.MemoryFileSystem;

// In memory page, used for testing
public class InMemoryPage {

  public static WikiPage makeRoot(String name) {
    MemoryFileSystem fileSystem = new MemoryFileSystem();
    WikiPageFactory factory = new FileSystemPageFactory(fileSystem, new MemoryVersionsController(fileSystem));
    return factory.makeRootPage(null, name);
  }

}
