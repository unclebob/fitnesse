package fitnesse.responders;

import fitnesse.responders.run.*;
import fitnesse.wiki.*;

public class WikiImportTestEventListener implements TestEventListener
{
	public static void register()
	{
		TestResponder.registerListener(new WikiImportTestEventListener(new WikiImporterFactory()));
	}

	private WikiImporterFactory importerFactory;

	public WikiImportTestEventListener(WikiImporterFactory importerFactory)
	{
		this.importerFactory = importerFactory;
	}

	public void notifyPreTest(TestResponder testResponder, PageData data) throws Exception
	{
		TestEventProcessor eventProcessor;
		if(testResponder instanceof SuiteResponder)
			eventProcessor = new SuiteEventProcessor();
		else
			eventProcessor = new TestEventProcessor();

		eventProcessor.run(testResponder, data);
	}

	private class TestEventProcessor implements WikiImporterClient
	{
		private TestResponder testResponder;
		private boolean errorOccured;
		protected WikiImporter wikiImporter;
		protected WikiPage wikiPage;
		protected PageData data;
		protected WikiImportProperty importProperty;

		public void run(TestResponder testResponder, PageData data) throws Exception
		{
			this.testResponder = testResponder;
			this.data = data;
			importProperty = WikiImportProperty.createFrom(data.getProperties());
			if(importProperty != null && importProperty.isAutoUpdate())
			{
				testResponder.addToResponse("<span class=\"meta\">Updating imported content...</span>");
				testResponder.addToResponse("<span class=\"meta\">");

				try
				{
					wikiImporter = importerFactory.newImporter(this);
					wikiImporter.parseUrl(importProperty.getSourceUrl());
					wikiPage = data.getWikiPage();

					doUpdating();

					if(!errorOccured)
						testResponder.addToResponse("done");

				}
				catch(Exception e)
				{
					pageImportError(data.getWikiPage(), e);
				}

				testResponder.addToResponse("</span>");
			}
		}

		protected void doUpdating() throws Exception
		{
			updatePagePassedIn();
		}

		protected void updatePagePassedIn() throws Exception
		{
			wikiImporter.importRemotePageContent(wikiPage);
			data.setContent(wikiPage.getData().getContent());
		}

		public void pageImported(WikiPage localPage) throws Exception
		{
		}

		public void pageImportError(WikiPage localPage, Exception e) throws Exception
		{
			errorOccured = true;
			testResponder.addToResponse(e.toString());
		}
	}

	private class SuiteEventProcessor extends TestEventProcessor
	{
		protected void doUpdating() throws Exception
		{
			if(!importProperty.isRoot())
				updatePagePassedIn();

			wikiImporter.importWiki(wikiPage);
		}
	}
}
