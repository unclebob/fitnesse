package fitnesse.responders;

import fitnesse.wiki.*;

import java.util.Iterator;

public class MockWikiImporter extends WikiImporter
{
	public static String mockContent = "mock importer content";
	public boolean fail;

	protected void importRemotePageContent(WikiPage localPage) throws Exception
	{
		if(fail)
			importerClient.pageImportError(localPage, new Exception("blah"));
		else
			setMockContent(localPage);
	}

	private void setMockContent(WikiPage localPage) throws Exception
	{
		PageData data = localPage.getData();
		data.setContent(mockContent);
		localPage.commit(data);
	}

	public void importWiki(WikiPage page) throws Exception
	{
		PageCrawler pageCrawler = page.getPageCrawler();
		for(Iterator iterator = page.getChildren().iterator(); iterator.hasNext();)
			pageCrawler.traverse((WikiPage) iterator.next(), this);
	}

	public void processPage(WikiPage page) throws Exception
	{
		setMockContent(page);
	}
}
