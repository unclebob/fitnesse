package fitnesse.updates;

import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.*;

public class WikiImportPropertyFormatUpdateTest extends UpdateTest
{
	protected Update makeUpdate() throws Exception
	{
		return new WikiImportPropertyFormatUpdate(updater);
	}

	public void testChangedFormat() throws Exception
	{
		addPropertyToPage(pageOne, "WikiImportSource", "http://import.source/value");
		addPropertyToPage(pageTwo, "WikiImportRoot", "http://import.root/value");

		update.doUpdate();

		checkWikiPageForNewFormat(pageOne, "WikiImportSource", false, "http://import.source/value");
		checkWikiPageForNewFormat(pageTwo, "WikiImportRoot", true, "http://import.root/value");
	}

	private void checkWikiPageForNewFormat(WikiPage page, String oldPropertyName, boolean root, String source) throws Exception
	{
		WikiPageProperties properties = page.getData().getProperties();
		assertFalse(properties.has(oldPropertyName));

		WikiImportProperty importProperty = WikiImportProperty.createFrom(properties);
		assertNotNull(importProperty);
		assertEquals(root, importProperty.isRoot());
		assertEquals(source, importProperty.getSourceUrl());
	}

	private void addPropertyToPage(WikiPage page, String propertyName, String propertyValue) throws Exception
	{
		PageData data = page.getData();
		WikiPageProperties properties = data.getProperties();
		properties.set(propertyName, propertyValue);
		page.commit(data);
	}
}
