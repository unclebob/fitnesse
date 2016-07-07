// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import java.io.File;
import java.util.Properties;

import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;

// In memory page, used for testing and instant pages (like GitFileVersionController's RecentChanges page).
public class InMemoryPage {

  public static WikiPage makeRoot(String name) {
    return makeRoot(name, null, new MemoryFileSystem());
  }

  public static WikiPage makeRoot(String name, MemoryFileSystem fileSystem) {
    return makeRoot(name, null, fileSystem);
  }

  public static WikiPage makeRoot(String name, Properties properties) {
    return makeRoot(name, properties, new MemoryFileSystem());
  }

  public static WikiPage makeRoot(String name, Properties properties, MemoryFileSystem fileSystem) {
    WikiPageFactory factory = newInstance(fileSystem);
    return factory.makePage(new File("."), name, null, new SystemVariableSource(properties));
  }

  public static WikiPageFactory newInstance() {
    MemoryFileSystem fileSystem = new MemoryFileSystem();
    return newInstance(fileSystem);
  }

  public static WikiPageFactory newInstance(FileSystem fileSystem) {
    return new FileSystemPageFactory(fileSystem, new MemoryVersionsController(fileSystem));
  }
}
