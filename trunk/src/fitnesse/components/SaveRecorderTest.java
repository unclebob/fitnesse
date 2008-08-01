// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.wiki.*;
import junit.framework.TestCase;

public class SaveRecorderTest extends TestCase
{
	public WikiPage somePage;
	public WikiPage root;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		somePage = crawler.addPage(root, PathParser.parse("SomePage"), "some page");
	}

	public void tearDown() throws Exception
	{
	}

	public void testTiming() throws Exception
	{
		PageData data = somePage.getData();
		long time = SaveRecorder.pageSaved(data);
		somePage.commit(data);
		assertEquals(true, SaveRecorder.changesShouldBeMerged(time - 1, 0, somePage.getData()));
		assertEquals(false, SaveRecorder.changesShouldBeMerged(time + 1, 0, somePage.getData()));
	}

	public void testDefaultValues() throws Exception
	{
		WikiPage neverSaved = crawler.addPage(root, PathParser.parse("NeverSaved"), "never saved");
		assertEquals(false, SaveRecorder.changesShouldBeMerged(12345, 0, neverSaved.getData()));
	}

}
