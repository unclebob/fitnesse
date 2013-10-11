// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.FileSystemPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystemPageFactory;
import org.junit.After;
import org.junit.Before;
import util.FileUtil;

public abstract class UpdateTestCase {
  public static final String rootName = "RooT";

  protected WikiPage root;
  protected Update update;
  protected UpdaterBase updater;
  protected WikiPage pageOne;
  protected WikiPage pageTwo;
  protected FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    root = new FileSystemPageFactory().makeRootPage(FitNesseUtil.base, rootName);
    context = FitNesseUtil.makeTestContext(root);

    FileUtil.makeDir(FitNesseUtil.base);

    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "some content");
    pageTwo = WikiPageUtil.addPage(pageOne, PathParser.parse("PageTwo"), "page two content");

    updater = new UpdaterBase(context);
    update = makeUpdate();

  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(FitNesseUtil.base);
  }

  protected Update makeUpdate() throws Exception {
    return null;
  }
}
