package fitnesse.responders;

public class WikiImporterFactory
{
	public WikiImporter newImporter(WikiImporterClient client)
	{
		return new WikiImporter(client);
	}
}

