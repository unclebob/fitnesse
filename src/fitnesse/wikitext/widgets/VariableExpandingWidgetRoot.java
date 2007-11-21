package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WidgetBuilder;

public class VariableExpandingWidgetRoot extends ParentWidget
{
	public VariableExpandingWidgetRoot(ParentWidget parent, String content) throws Exception
	{
		super(parent);
		if(content != null) addChildWidgets(content);
	}

	public WidgetBuilder getBuilder()
	{
		return WidgetBuilder.literalAndVariableWidgetBuilder;
	}

	public boolean doEscaping()
	{
		return false;
	}

	public String render() throws Exception
	{
		return "";
	}

	protected void addToParent()
	{
	}
}
