package fitnesse.wikitext.widgets;

public class BlankParentWidget extends ParentWidget
{
	public BlankParentWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		addChildWidgets(text);
	}

	public String render() throws Exception
	{
		return "";
	}

	public String asWikiText() throws Exception
	{
		return "";
	}
}
