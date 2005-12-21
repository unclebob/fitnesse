package fitnesse.responders;

import junit.framework.TestCase;
import fitnesse.wiki.WikiPageProperty;

public class WikiImportPropertyTest extends TestCase
{
	public void testSource() throws Exception
	{
		WikiImportProperty property = new WikiImportProperty("import source");
		assertEquals("import source", property.getSource());
		assertEquals("import source", property.get("Source"));
	}

	public void testIsRoot() throws Exception
	{
		WikiImportProperty property = new WikiImportProperty("");

		assertFalse(property.isRoot());
		assertFalse(property.has("IsRoot"));

		property.setRoot(true);

		assertTrue(property.isRoot());
		assertTrue(property.has("IsRoot"));
	}

	public void testFailedCreateFromProperty() throws Exception
	{
		assertNull(WikiImportProperty.createFrom(new WikiPageProperty()));
	}

	public void testCreateFromProperty() throws Exception
	{
		WikiPageProperty rawProperty = new WikiPageProperty();
		WikiPageProperty rawImportProperty = rawProperty.set(WikiImportProperty.PROPERTY_NAME);
		rawImportProperty.set("IsRoot");
		rawImportProperty.set("Source", "some source");

		WikiImportProperty importProperty = WikiImportProperty.createFrom(rawProperty);
		assertEquals("some source", importProperty.getSource());
		assertTrue(importProperty.isRoot());
	}

	public void testAddtoProperty() throws Exception
	{
		WikiPageProperty rootProperty = new WikiPageProperty();

		WikiImportProperty importProperty = new WikiImportProperty("some source");
		importProperty.setRoot(true);
		importProperty.addTo(rootProperty);

		WikiImportProperty importProperty2 = WikiImportProperty.createFrom(rootProperty);
		assertEquals("some source", importProperty2.getSource());
		assertTrue(importProperty2.isRoot());

	}
}
