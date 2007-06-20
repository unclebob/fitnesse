// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetBuilder;

import java.util.*;

public class WidgetRoot extends ParentWidget
{
	private Map<String, String> variables = new HashMap<String, String>();
	private WidgetBuilder builder;
	private WikiPage page;
	private boolean doEscaping = true;
	private List<String> literals = new LinkedList<String>();

	public WidgetRoot(WikiPage page) throws Exception
	{
		this("", page, WidgetBuilder.htmlWidgetBuilder);
	}

	public WidgetRoot(String value, WikiPage page) throws Exception
	{
		this(value, page, WidgetBuilder.htmlWidgetBuilder);
	}

	public WidgetRoot(String value, WikiPage page, WidgetBuilder builder) throws Exception
	{
		super(null);
		this.page = page;
		this.builder = builder;
		if(value != null)
			buildWidgets(value);
	}

	public WidgetRoot(PagePointer pagePointer) throws Exception
	{
		this("", pagePointer, WidgetBuilder.htmlWidgetBuilder);
	}

	public WidgetRoot(String value, PagePointer pagePointer) throws Exception
	{
		this(value, pagePointer, WidgetBuilder.htmlWidgetBuilder);
	}

	public WidgetRoot(String value, PagePointer pagePointer, WidgetBuilder builder) throws Exception
	{
		super(null);
		this.page = pagePointer.getPage();
		this.builder = builder;
		if(value != null)
			buildWidgets(value);
	}

	public WidgetBuilder getBuilder()
	{
		return builder;
	}

	protected void buildWidgets(String value) throws Exception
	{
		String nonLiteralContent = processLiterals(value);
		addChildWidgets(nonLiteralContent);
	}

	public String render() throws Exception
	{
		return childHtml();
	}

	public String getVariable(String key) throws Exception
	{
		String value = (String) variables.get(key);

		WikiPage page = getWikiPage();
		while(value == null && !page.getPageCrawler().isRoot(page))
		{
			page = page.getParent();
			value = page.getData().getVariable(key);
		}
		if(value == null)
		{
			value = System.getProperty(key);
		}
		return value;
	}

	public void addVariable(String key, String value)
	{
		variables.put(key, value);
	}

	public int defineLiteral(String literal)
	{
		int literalNumber = literals.size();
		literals.add(literal);
		return literalNumber;
	}

	public String getLiteral(int literalNumber)
	{
		if(literalNumber >= literals.size())
			return "literal(" + literalNumber + ") not found.";
		return (String) literals.get(literalNumber);
	}

	public WikiPage getWikiPage()
	{
		return page;
	}

	public void setEscaping(boolean value)
	{
		doEscaping = value;
	}

	public boolean doEscaping()
	{
		return doEscaping;
	}

	public List getLiterals()
	{
		return literals;
	}

	public void setLiterals(List<String> literals)
	{
		this.literals = literals;
	}

	public String asWikiText() throws Exception
	{
		return childWikiText();
	}
}

