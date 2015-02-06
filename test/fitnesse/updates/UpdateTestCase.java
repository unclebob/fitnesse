// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import java.io.File;

import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystemPageFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class UpdateTestCase {
  public static final String rootName = "RooT";

  @Rule
  public TemporaryFolder testRoot = new TemporaryFolder();
  protected File testDir;

  protected WikiPage root;
  protected Update update;
  protected UpdaterBase updater;
  protected WikiPage pageOne;
  protected WikiPage pageTwo;
  protected FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    testDir = testRoot.newFolder("TestDir");
    root = new FileSystemPageFactory().makePage(testDir, rootName, null, new SystemVariableSource());
    context = FitNesseUtil.makeTestContext(new FileSystemPageFactory(), testRoot.getRoot().getPath(), testDir.getName(), 0);

    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "some content");
    pageTwo = WikiPageUtil.addPage(pageOne, PathParser.parse("PageTwo"), "page two content");

    updater = new UpdaterBase(context);
    update = makeUpdate();

  }

  @After
  public void tearDown() throws Exception {
  }

  protected Update makeUpdate() throws Exception {
    return null;
  }
}
