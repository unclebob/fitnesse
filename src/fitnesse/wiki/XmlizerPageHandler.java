package fitnesse.wiki;

public interface XmlizerPageHandler
{
	void pageAdded(WikiPage newPage) throws Exception;

	void exitPage();
}
