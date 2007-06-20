package fitnesse.responders;

public class MockWikiImporterFactory extends WikiImporterFactory
{
	public MockWikiImporter mockWikiImporter = new MockWikiImporter();

	public WikiImporter newImporter(WikiImporterClient client)
	{
		mockWikiImporter.setWikiImporterClient(client);
		return mockWikiImporter;
	}
}
