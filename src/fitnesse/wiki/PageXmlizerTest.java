package fitnesse.wiki;

import java.io.*;
import java.util.*;
import fitnesse.testutil.RegexTest;
import fitnesse.util.XmlUtil;
import org.w3c.dom.Document;

public class PageXmlizerTest extends RegexTest
{
	private PageXmlizer xmlizer;
	private WikiPage root;;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		xmlizer = new PageXmlizer();
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
	}

	public void tearDown() throws Exception
	{
	}

	public void testXmlizeOneWikiPage() throws Exception
	{
		Document doc = xmlizer.xmlize(root);
		String value = XmlUtil.xmlAsString(doc);

		assertSubString("<page>", value);
		assertSubString("</page>", value);
		assertSubString("<name>RooT</name>", value);
	}

	public void testDeXmlizeOneWikiPage() throws Exception
	{
		Document doc = xmlizer.xmlize(root);
		xmlizer.deXmlize(doc, root, new MockXmlizerPageHandler());

		List children = root.getChildren();
		assertEquals(1, children.size());
		WikiPage page = (WikiPage)children.get(0);
		assertEquals("RooT", page.getName());
	}

	public void testXmlizeTwoPages() throws Exception
	{
		root.addChildPage("PageOne");
		Document doc = xmlizer.xmlize(root);
		String value = XmlUtil.xmlAsString(doc);

		assertSubString("<name>RooT</name>", value);
		assertSubString("<name>PageOne</name>", value);
	}

	public void testDeXmlizingTwoPages() throws Exception
	{
		root.addChildPage("PageOne");
		xmlizer.deXmlize(xmlizer.xmlize(root), root, new MockXmlizerPageHandler());

		assertEquals(2, root.getChildren().size());
		WikiPage marshaledRoot = root.getChildPage("RooT");
		assertNotNull(marshaledRoot);

		assertEquals(1, marshaledRoot.getChildren().size());
		assertNotNull(marshaledRoot.getChildPage("PageOne"));
	}

	public void testXmlizingAnEntireTree() throws Exception
	{
		makeFamilyOfPages();
		Document doc = xmlizer.xmlize(root);

		String value = XmlUtil.xmlAsString(doc);

		assertSubString("PageA", value);
		assertSubString("ChildOneA", value);
		assertSubString("GrandChildA", value);
		assertSubString("GreatGrandChildA", value);
		assertSubString("ChildTwoA", value);
		assertSubString("GrandChildTwoA", value);
		assertSubString("PageB", value);
		assertSubString("ChildOneB", value);
		assertSubString("GrandChildB", value);
		assertSubString("PageC", value);
	}

	public void testDeXmlizingEntireTree() throws Exception
	{
		makeFamilyOfPages();
		xmlizer.deXmlize(xmlizer.xmlize(root), root, new MockXmlizerPageHandler());

		assertEquals(4, root.getChildren().size());
		WikiPage marshaledRoot = root.getChildPage("RooT");
		assertEquals(3, marshaledRoot.getChildren().size());
		WikiPage pageA = marshaledRoot.getChildPage("PageA");
		assertEquals(2, pageA.getChildren().size());
		WikiPage childOneA = pageA.getChildPage("ChildOneA");
		WikiPage grandChildA = childOneA.getChildPage("GrandChildA");
		WikiPage greatGrandChildA = grandChildA.getChildPage("GreatGrandChildA");
		assertNotNull(greatGrandChildA);

		assertNotNull(crawler.getPage(root, PathParser.parse("RooT.PageB.ChildOneB.GrandChildB")));
		assertNotNull(crawler.getPage(root, PathParser.parse("RooT.PageC")));
	}

	public void testDeXmlizeEntireTreeTwice() throws Exception
	{
		makeFamilyOfPages();
		Document doc = xmlizer.xmlize(root);
		xmlizer.deXmlize(doc, root, new MockXmlizerPageHandler());
		xmlizer.deXmlize(doc, root, new MockXmlizerPageHandler());

		assertEquals(4, root.getChildren().size());
		WikiPage marshaledRoot = root.getChildPage("RooT");
		assertEquals(3, marshaledRoot.getChildren().size());
		WikiPage pageA = marshaledRoot.getChildPage("PageA");
		assertEquals(2, pageA.getChildren().size());
	}

	public void testDeXmlizeSkippingRootLevel() throws Exception
	{
		makeFamilyOfPages();
		WikiPage pageC = root.getChildPage("PageC");
		xmlizer.deXmlizeSkippingRootLevel(xmlizer.xmlize(root), pageC, new MockXmlizerPageHandler());

		assertEquals(3, pageC.getChildren().size());
		WikiPage pageA = pageC.getChildPage("PageA");
		assertEquals(2, pageA.getChildren().size());
		WikiPage childOneA = pageA.getChildPage("ChildOneA");
		WikiPage grandChildA = childOneA.getChildPage("GrandChildA");
		WikiPage greatGrandChildA = grandChildA.getChildPage("GreatGrandChildA");
		assertNotNull(greatGrandChildA);

		assertNotNull(crawler.getPage(pageC, PathParser.parse("PageB.ChildOneB.GrandChildB")));
		assertNotNull(crawler.getPage(pageC, PathParser.parse("PageC")));
	}

	public void testUsageOfHandler() throws Exception
	{
		makeFamilyOfPages();
		MockXmlizerPageHandler handler = new MockXmlizerPageHandler();
		xmlizer.deXmlize(xmlizer.xmlize(root), root, handler);

		assertEquals(11, handler.adds.size());
		assertTrue(handler.adds.contains("RooT"));
		assertTrue(handler.adds.contains("PageA"));
		assertTrue(handler.adds.contains("ChildOneA"));
		assertTrue(handler.adds.contains("GrandChildA"));
		assertTrue(handler.adds.contains("GreatGrandChildA"));
		assertTrue(handler.adds.contains("ChildTwoA"));
		assertTrue(handler.adds.contains("GrandChildTwoA"));
		assertTrue(handler.adds.contains("PageB"));
		assertTrue(handler.adds.contains("ChildOneB"));
		assertTrue(handler.adds.contains("GrandChildB"));
		assertTrue(handler.adds.contains("PageC"));

		assertEquals(11, handler.exits);
	}

	private void makeFamilyOfPages() throws Exception
	{
		addPage("PageA", "page a");
		addPage("PageA.ChildOneA", "child one a");
		addPage("PageA.ChildOneA.GrandChildA", "grand child a");
		addPage("PageA.ChildOneA.GrandChildA.GreatGrandChildA", "great grand child a");
		addPage("PageA.ChildTwoA", "child two a");
		addPage("PageA.ChildTwoA.GrandChildTwoA", "grand child two a");

		addPage("PageB", "page b");
		addPage("PageB.ChildOneB", "child one b");
		addPage("PageB.ChildOneB.GrandChildB", "grand child b");

		addPage("PageC", "page c");
	}

	private void addPage(String path, String content)
	  throws Exception
	{
		crawler.addPage(root, PathParser.parse(path), content);
	}

	public void testXmlizingData() throws Exception
	{
		PageData data = new PageData(root);
		data.setContent("this is some content.");
		WikiPageProperties properties = data.getProperties();

		Document doc = xmlizer.xmlize(data);
		String marshaledValue = XmlUtil.xmlAsString(doc);

		assertSubString("<data>", marshaledValue);
		assertSubString("CDATA", marshaledValue);
		assertSubString("this is some content", marshaledValue);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		properties.save(output);
		String[] propertyLines = output.toString().split("\n");
		for(int i = 0; i < propertyLines.length; i++)
		{
			String propertyLine = propertyLines[i].trim();
			assertSubString(propertyLine, marshaledValue);
		}
	}

	public void testDeXmlizingPageData() throws Exception
	{
		PageData data = new PageData(root);
		data.setContent("this is some content.");
		WikiPageProperties properties = data.getProperties();

		PageData receivedData = xmlizer.deXmlizeData(xmlizer.xmlize(data));
		assertNotSame(data, receivedData);

		assertEquals("this is some content.", receivedData.getContent());
		WikiPageProperties receivedProperties = receivedData.getProperties();
		assertNotSame(properties, receivedProperties);
		assertEquals(properties.toString(), receivedProperties.toString());
	}
}
