// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.wiki.*;
import fitnesse.http.*;
import fitnesse.*;
import fitnesse.testutil.RegexTest;
import fitnesse.util.FileUtil;

import java.io.*;

public class SerializedPageResponderTest extends RegexTest
{
	private final String RootPath = "TestRooT";
	private PageCrawler crawler;
	private WikiPage root;
	private MockRequest request;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
		request = new MockRequest();
	}

	public void tearDown() throws Exception
	{
		FileUtil.deleteFileSystemDirectory(RootPath);
	}

	public void testWithInMemory() throws Exception
	{
		Object obj = doSetUpWith(root, "bones");
		doTestWith(obj);

	}

	public void testWithFileSystem() throws Exception
	{
		root = FileSystemPage.makeRoot(".", RootPath);
		Object obj = doSetUpWith(root, "bones");
		FileUtil.deleteFileSystemDirectory(RootPath);
		doTestWith(obj);
	}

	private void doTestWith(Object obj) throws Exception
	{
		assertNotNull(obj);
		assertEquals(true, obj instanceof ProxyPage);
		WikiPage page = (WikiPage) obj;
		assertEquals("PageOne", page.getName());
	}

	private Object doSetUpWith(WikiPage root, String proxyType) throws Exception
	{
		WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"), "this is page one");
		PageData data = page1.getData();
		data.setAttribute("Attr1", "true");
		page1.commit(data);
		crawler.addPage(page1, PathParser.parse("ChildOne"), "this is child one");

		request.addInput("type", proxyType);
		request.setResource("PageOne");

		return getObject(root, request);
	}

	private Object getObject(WikiPage root, MockRequest request) throws Exception
	{
		Responder responder = new SerializedPageResponder();
		SimpleResponse response = (SimpleResponse)responder.makeResponse(new FitNesseContext(root), request);

		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(response.getContentBytes()));
		Object obj = ois.readObject();
		return obj;
	}

	public void testGetContentAndAttributes() throws Exception
	{
		Object obj = doSetUpWith(root, "meat");
		assertNotNull(obj);
		assertTrue(obj instanceof PageData);
		PageData data = (PageData) obj;

		assertEquals("this is page one", data.getContent());

		WikiPageProperties props = data.getProperties();
		assertEquals("true", props.get("Attr1"));
	}

	public void testGetVersionOfPageData() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"), "some content");
		VersionInfo commitRecord = page.commit(page.getData());

		request.addInput("type", "meat");
		request.addInput("version", commitRecord.getName());
		request.setResource("PageOne");

		Object obj = getObject(root, request);
		assertEquals(PageData.class, obj.getClass());
		PageData data = (PageData) obj;
		assertEquals("some content", data.getContent());
	}

	public void testGetPageHieratchyAsXml() throws Exception
	{
		crawler.addPage(root, PathParser.parse("PageOne"));
		crawler.addPage(root, PathParser.parse("PageOne.ChildOne"));
		crawler.addPage(root, PathParser.parse("PageTwo"));

		request.setResource("root");
		request.addInput("type", "pages");
		Responder responder = new SerializedPageResponder();
		SimpleResponse response = (SimpleResponse)responder.makeResponse(new FitNesseContext(root), request);
		String xml = response.getContent();

		assertEquals("text/xml", response.getContentType());
		assertSubString("<name>PageOne</name>", xml);
		assertSubString("<name>PageTwo</name>", xml);
		assertSubString("<name>ChildOne</name>", xml);
	}

	public void testGetDataAsHtml() throws Exception
	{
		crawler.addPage(root, PathParser.parse("TestPageOne"), "test page");

		request.setResource("TestPageOne");
		request.addInput("type", "data");
		Responder responder = new SerializedPageResponder();
		SimpleResponse response = (SimpleResponse)responder.makeResponse(new FitNesseContext(root), request);
		String xml = response.getContent();

		assertEquals("text/xml", response.getContentType());
		assertSubString("test page", xml);
		assertSubString("<Test/>", xml);
	}
}
