// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.wikitext.widgets.*;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.responders.run.*;
import fitnesse.responders.editing.EditResponder;
import fitnesse.components.SaveRecorder;

import java.util.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;

public class PageData implements Serializable
{
	public static WidgetBuilder classpathWidgetBuilder = new WidgetBuilder(new Class[]{ClasspathWidget.class});
	public static WidgetBuilder fixtureWidgetBuilder = new WidgetBuilder(new Class[]{FixtureWidget.class});
	public static WidgetBuilder xrefWidgetBuilder = new WidgetBuilder(new Class[]{XRefWidget.class});
	public static WidgetBuilder variableDefinitionWidgetBuilder = new WidgetBuilder(new Class[]{VariableDefinitionWidget.class});

	private transient WikiPage wikiPage;
	private String content;
	private WikiPageProperties properties = new WikiPageProperties();
	private Set versions;
	private WidgetRoot variableRoot;

	public static SimpleDateFormat makeVersionTimeFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	public PageData(WikiPage page) throws Exception
	{
		wikiPage = page;
		initializeAttributes();
		versions = new HashSet();
	}

	public PageData(WikiPage page, String content) throws Exception
	{
		this(page);
		setContent(content);
	}

	public PageData(PageData data) throws Exception
	{
		this(data.getWikiPage());
		wikiPage = data.wikiPage;
		content = data.content;
		properties = new WikiPageProperties(data.properties);
		versions.addAll(data.versions);
	}

	public String getStringOfAllAttributes()
	{
		return properties.toString();
	}

	public void initializeAttributes() throws Exception
	{
		properties.set("Edit", "true");
		properties.set("Search", "true");
		properties.set("Versions", "true");
		properties.set("Properties", "true");
		properties.set("Refactor", "true");
		properties.set("WhereUsed", "true");
		properties.set("Files", "true");
		properties.set(EditResponder.TICKET_ID, SaveRecorder.newTicket() + "");
		properties.set("LastModified", makeVersionTimeFormat().format(new Date()));

		final String pageName = wikiPage.getName();
		if(pageName.startsWith("Test"))
			properties.set("Test", "true");
		if(pageName.startsWith("Suite") &&
		  !pageName.equals(SuiteResponder.SUITE_SETUP_NAME) &&
		  !pageName.equals(SuiteResponder.SUITE_TEARDOWN_NAME))
		{
			properties.set("Suite", "true");
		}
	}

	public WikiPageProperties getProperties() throws Exception
	{
		return properties;
	}

	public String getAttribute(String key) throws Exception
	{
		return properties.get(key);
	}

	public void removeAttribute(String key) throws Exception
	{
		properties.remove(key);
	}

	public void setAttribute(String key, String value) throws Exception
	{
		properties.set(key, value);
	}

	public void setAttribute(String key) throws Exception
	{
		properties.set(key);
	}

	public boolean hasAttribute(String attribute) throws Exception
	{
		return properties.has(attribute);
	}

	public void setProperties(WikiPageProperties properties)
	{
		this.properties = properties;
	}

	public String getContent() throws Exception
	{
		return content;
	}

	public void setContent(String content) throws Exception
	{
		this.content = content;
	}

	public String getHtml() throws Exception
	{
		return processHTMLWidgets(getContent(), wikiPage);
	}

	public String getHtml(WikiPage context) throws Exception
	{
		return processHTMLWidgets(getContent(), context);
	}

	public String getVariable(String name) throws Exception
	{
		if(variableRoot == null)
		{
			variableRoot = new TextIgnoringWidgetRoot(getContent(), wikiPage, variableDefinitionWidgetBuilder);
			variableRoot.render();
		}
		return variableRoot.getVariable(name);
	}

	private String processHTMLWidgets(String content, WikiPage context) throws Exception
	{
		WidgetRoot root = new WidgetRoot(content, context, WidgetBuilder.htmlWidgetBuilder);
		return root.render();
	}

	public void setWikiPage(WikiPage page)
	{
		wikiPage = page;
	}

	public WikiPage getWikiPage()
	{
		return wikiPage;
	}

	public List getClasspaths() throws Exception
	{
		return getTextOfWidgets(classpathWidgetBuilder);
	}

	public List getFixtureNames() throws Exception
	{
		return getTextOfWidgets(fixtureWidgetBuilder);
	}

	public List getXrefPages() throws Exception
	{
		return getTextOfWidgets(xrefWidgetBuilder);
	}

	private List getTextOfWidgets(WidgetBuilder builder) throws Exception
	{
		WidgetRoot root = new TextIgnoringWidgetRoot(getContent(), wikiPage, builder);
		List widgets = root.getChildren();
		List values = new ArrayList();
		for(Iterator iterator = widgets.iterator(); iterator.hasNext();)
		{
			WidgetWithTextArgument widget = (WidgetWithTextArgument) iterator.next();
			values.add(widget.getText());
		}
		return values;
	}

	public Set getVersions()
	{
		return versions;
	}

	public void addVersions(Collection newVersions)
	{
		versions.addAll(newVersions);
	}

	public Date getLastModificationTime() throws Exception
	{
		String dateStr = (String) properties.get("LastModified");
		if(dateStr == null)
			return new Date();
		else
			return makeVersionTimeFormat().parse(dateStr);
	}

	public void setLastModificationTime(Date date)
	{
		properties.set("LastModified", makeVersionTimeFormat().format(date));
	}
}
