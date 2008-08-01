package fitnesse.responders;

import fitnesse.wiki.WikiPage;

public interface WikiImporterClient
{
	void pageImported(WikiPage localPage) throws Exception;

	void pageImportError(WikiPage localPage, Exception e) throws Exception;
}
