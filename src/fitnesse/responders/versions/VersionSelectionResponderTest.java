// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.versions;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

import java.util.*;

public class VersionSelectionResponderTest extends RegexTest
{
	private WikiPage page;
	private WikiPage root;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		page = root.getPageCrawler().addPage(root, PathParser.parse("PageOne"), "some content");
	}

	public void tearDown() throws Exception
	{
	}

	public void testGetVersionsList() throws Exception
	{
		Set set = new HashSet();
		VersionInfo v1 = new VersionInfo("1-12345678901234");
		VersionInfo v2 = new VersionInfo("2-45612345678901");
		VersionInfo v3 = new VersionInfo("3-11112345678901");
		VersionInfo v4 = new VersionInfo("4-12212345465679");
		set.add(v1);
		set.add(v2);
		set.add(v3);
		set.add(v4);

		PageData data = new PageData(page);
		data.addVersions(set);

		List list = VersionSelectionResponder.getVersionsList(data);
		assertEquals(v3, list.get(3));
		assertEquals(v4, list.get(2));
		assertEquals(v1, list.get(1));
		assertEquals(v2, list.get(0));
	}

	public void testConvertVersionNameToAge() throws Exception
	{
		Date now = new GregorianCalendar(2003, 0, 1, 00, 00, 01).getTime();
		Date tenSeconds = new GregorianCalendar(2003, 0, 1, 00, 00, 11).getTime();
		Date twoMinutes = new GregorianCalendar(2003, 0, 1, 00, 02, 01).getTime();
		Date fiftyNineSecs = new GregorianCalendar(2003, 0, 1, 00, 01, 00).getTime();
		Date oneHour = new GregorianCalendar(2003, 0, 1, 01, 00, 01).getTime();
		Date fiveDays = new GregorianCalendar(2003, 0, 6, 00, 00, 01).getTime();
		Date years = new GregorianCalendar(2024, 0, 1, 00, 00, 01).getTime();

		assertEquals("10 seconds", VersionSelectionResponder.howLongAgoString(now, tenSeconds));
		assertEquals("2 minutes", VersionSelectionResponder.howLongAgoString(now, twoMinutes));
		assertEquals("59 seconds", VersionSelectionResponder.howLongAgoString(now, fiftyNineSecs));
		assertEquals("1 hour", VersionSelectionResponder.howLongAgoString(now, oneHour));
		assertEquals("5 days", VersionSelectionResponder.howLongAgoString(now, fiveDays));
		assertEquals("21 years", VersionSelectionResponder.howLongAgoString(now, years));
	}

	public void testMakeReponder() throws Exception
	{
		MockRequest request = new MockRequest();
		request.setResource("PageOne");

		Responder responder = new VersionSelectionResponder();
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

		String content = response.getContent();
		assertSubString("<input", content);
		assertSubString("name=\"version\"", content);
		assertSubString("<form", content);
		assertSubString("action=\"PageOne\"", content);
		assertSubString("name=\"responder\"", content);
		assertSubString(" value=\"viewVersion\"", content);
	}
}
