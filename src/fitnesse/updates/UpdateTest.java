// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.util.FileUtil;
import fitnesse.wiki.*;
import junit.framework.TestCase;

public abstract class UpdateTest extends TestCase
{
	public static final String testDir = "testDir";
	public static final String rootName = "RooT";

	protected WikiPage root;
	protected Update update;
	protected Updater updater;
	protected WikiPage pageOne;
	protected WikiPage pageTwo;
	protected FitNesseContext context;
	protected PageCrawler crawler;

	public void setUp() throws Exception
	{
		context = new FitNesseContext();
		context.rootPath = testDir;
		context.rootPageName = rootName;
		context.rootPagePath = testDir + "/" + rootName;

		FileUtil.makeDir(testDir);
		root = FileSystemPage.makeRoot(context.rootPath, context.rootPageName);
		crawler = root.getPageCrawler();
		context.root = root;

		pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "some content");
		pageTwo = crawler.addPage(pageOne, PathParser.parse("PageTwo"), "page two content");

		updater = new Updater(context);
		update = makeUpdate();
	}

	public void tearDown() throws Exception
	{
		FileUtil.deleteFileSystemDirectory(testDir);
	}

	protected Update makeUpdate() throws Exception
	{
		return null;
	}

	;
}
