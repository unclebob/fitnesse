// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wiki.storage.MemoryFileSystem;

// In memory page, used for testing
public class InMemoryPage {

  public static WikiPage makeRoot(String name) {
    WikiPageFactory factory = new FileSystemPageFactory(new MemoryFileSystem(), new MemoryVersionsController());
    return factory.makeRootPage(null, name);
  }

}
