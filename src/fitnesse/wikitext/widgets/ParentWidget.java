// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.*;

import java.util.*;

public abstract class ParentWidget extends WikiWidget
{
	protected LinkedList children = new LinkedList();
	private int currentChild = 0;

	public ParentWidget(ParentWidget parent)
	{
		super(parent);
	}

	public void reset()
	{
		children.clear();
		currentChild = 0;
	}

	public void addChild(WikiWidget widget)
	{
		children.add(widget);
	}

	public int numberOfChildren()
	{
		return children.size();
	}

	public List getChildren()
	{
		return children;
	}

	public WikiWidget nextChild()
	{
		if(hasNextChild())
			return (WikiWidget) children.get(currentChild++);
		else
			throw new ArrayIndexOutOfBoundsException("No next child exists");
	}

	public boolean hasNextChild()
	{
		return (currentChild < numberOfChildren());
	}

	public String childHtml() throws Exception
	{
		currentChild = 0;
		StringBuffer html = new StringBuffer();
		while(hasNextChild())
		{
			WikiWidget child = nextChild();
			html.append(child.render());
		}

		return html.toString();
	}

	public String childWikiText() throws Exception
	{
		currentChild = 0;
		StringBuffer wikiText = new StringBuffer();
		while(hasNextChild())
		{
			WikiWidget child = nextChild();
			wikiText.append(child.asWikiText());
		}

		return wikiText.toString();

	}

	public int defineLiteral(String literal)
	{
		return parent.defineLiteral(literal);
	}

	public String getLiteral(int literalNumber)
	{
		return parent.getLiteral(literalNumber);
	}

	public void addVariable(String key, String value)
	{
		parent.addVariable(key, value);
	}

	public String getVariable(String key) throws Exception
	{
		return parent.getVariable(key);
	}

	public void addChildWidgets(String value) throws Exception
	{
		getBuilder().addChildWidgets(value, this);
	}

	public WidgetBuilder getBuilder()
	{
		return parent.getBuilder();
	}

	public boolean doEscaping()
	{
		return parent.doEscaping();
	}

	public boolean preProcessingComplete()
	{
		return (children.size() == 1 && (children.get(0) instanceof TextWidget));
	}

	public void acceptVisitor(WidgetVisitor visitor) throws Exception
	{
		visitor.visit(this);
		currentChild = 0;
		while(hasNextChild())
		{
			WikiWidget child = nextChild();
			child.acceptVisitor(visitor);
		}
	}

	public String processLiterals(String value) throws Exception
	{
		return new LiteralProcessingWidgetRoot(this, value).childHtml();
	}

	public static WidgetBuilder preprocessingLiteralWidgetBuilder = new WidgetBuilder(new Class[]{PreProcessorLiteralWidget.class});

	public static class LiteralProcessingWidgetRoot extends ParentWidget
	{
		public LiteralProcessingWidgetRoot(ParentWidget parent, String content) throws Exception
		{
			super(parent);
			if(content != null)
				addChildWidgets(content);
		}

		public WidgetBuilder getBuilder()
		{
			return preprocessingLiteralWidgetBuilder;
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
}

