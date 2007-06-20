// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.*;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class ComponentFactory
{
	private final String endl = System.getProperty("line.separator");
	public static final String PROPERTIES_FILE = "plugins.properties";
	public static final String WIKI_PAGE_CLASS = "WikiPage";
	public static final String HTML_PAGE_FACTORY = "HtmlPageFactory";
	public static final String RESPONDERS = "Responders";
	public static final String WIKI_WIDGETS = "WikiWidgets";
	public static final String WIKI_WIDGET_INTERCEPTORS = "WikiWidgetInterceptors";
	public static final String AUTHENTICATOR = "Authenticator";
	public static final String CONTENT_FILTER = "ContentFilter";

	private Properties loadedProperties;
	private String propertiesLocation;

	public ComponentFactory(String propertiesLocation)
	{
		this(propertiesLocation, new Properties());
	}

	public ComponentFactory(String propertiesLocation, Properties properties)
	{
		this.propertiesLocation = propertiesLocation;
		this.loadedProperties = properties;
		loadProperties();
	}

	public void loadProperties()
	{
		try
		{
			loadedProperties.load(new FileInputStream(propertiesLocation + "/" + PROPERTIES_FILE));
		}
		catch(IOException e)
		{
			// No properties files means all defaults are loaded
		}
	}

	private Object createComponent(String componentType) throws Exception
	{
		String componentClassName = loadedProperties.getProperty(componentType);
		if(componentClassName != null)
		{
			Class componentClass = Class.forName(componentClassName);
			Constructor constructor = componentClass.getConstructor(new Class[]{Properties.class});
			return constructor.newInstance(new Object[]{loadedProperties});
		}
		return null;
	}

	public WikiPage getRootPage(WikiPage defaultPage) throws Exception
	{
		String rootPageClassName = loadedProperties.getProperty(WIKI_PAGE_CLASS);
		if(rootPageClassName != null)
		{
			Class rootPageClass = Class.forName(rootPageClassName);
			Method constructorMethod = rootPageClass.getMethod("makeRoot", new Class[]{Properties.class});
			return (WikiPage) constructorMethod.invoke(rootPageClass, new Object[]{loadedProperties});
		}
		else
			return defaultPage;
	}

	public HtmlPageFactory getHtmlPageFactory(HtmlPageFactory defaultPageFactory) throws Exception
	{
		HtmlPageFactory htmlPageFactory = (HtmlPageFactory) createComponent(HTML_PAGE_FACTORY);
		return htmlPageFactory == null ? defaultPageFactory : htmlPageFactory;
	}

	public String loadResponderPlugins(ResponderFactory responderFactory) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		String responderList = loadedProperties.getProperty(RESPONDERS);
		if(responderList != null)
		{
			buffer.append("\tCustom responders loaded:").append(endl);
			String[] responderPairs = responderList.split(",");
			for(int i = 0; i < responderPairs.length; i++)
			{
				String pair = responderPairs[i].trim();
				String[] values = pair.split(":");
				String responderKey = values[0];
				Class responderClass = Class.forName(values[1]);
				responderFactory.addResponder(responderKey, responderClass);
				buffer.append("\t\t" + responderKey + ":" + responderClass.getName()).append(endl);
			}
		}
		return buffer.toString();
	}

	public Authenticator getAuthenticator(Authenticator defaultAuthenticator) throws Exception
	{
		Authenticator authenticator = (Authenticator) createComponent(AUTHENTICATOR);
		return authenticator == null ? defaultAuthenticator : authenticator;
	}

	public String loadWikiWidgetPlugins() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		String widgetList = loadedProperties.getProperty(WIKI_WIDGETS);
		if(widgetList != null)
		{
			List widgetClasses = new ArrayList();
			buffer.append("\tCustom wiki widgets loaded:").append(endl);
			String[] widgetNames = widgetList.split(",");
			for(int i = 0; i < widgetNames.length; i++)
			{
				String widgetName = widgetNames[i].trim();
				Class widgetClass = Class.forName(widgetName);
				widgetClasses.add(widgetClass);
				buffer.append("\t\t" + widgetClass.getName()).append(endl);
			}
			appendExistingWidgets(widgetClasses);
			Class[] widgetClassesArray = (Class[]) widgetClasses.toArray(new Class[]{});
			WidgetBuilder.htmlWidgetBuilder = new WidgetBuilder(widgetClassesArray);
		}

		return buffer.toString();
	}

	public String loadWikiWidgetInterceptors() throws Exception
	{
		StringBuffer buffer = new StringBuffer();

		String widgetInterceptorList = loadedProperties.getProperty(WIKI_WIDGET_INTERCEPTORS);
		if(widgetInterceptorList != null)
		{
			buffer.append("\tCustom wiki widget interceptors loaded:").append(endl);
			for(String interceptorClass : widgetInterceptorList.split(","))
			{
				WidgetBuilder.htmlWidgetBuilder.addInterceptor((WidgetInterceptor) Class.forName(interceptorClass).newInstance());
				buffer.append("\t\t").append(interceptorClass).append(endl);
			}
		}
		return buffer.toString();
	}

	private void appendExistingWidgets(List widgetClasses)
	{
		for(int i = 0; i < WidgetBuilder.htmlWidgetClasses.length; i++)
		{
			Class htmlWidgetClass = WidgetBuilder.htmlWidgetClasses[i];
			widgetClasses.add(htmlWidgetClass);
		}
	}

	public String loadContentFilter() throws Exception
	{
		ContentFilter filter = (ContentFilter) createComponent(CONTENT_FILTER);
		if(filter != null)
		{
			SaveResponder.contentFilter = filter;
			return "\tContent filter installed: " + filter.getClass().getName() + "\n";
		}
		return "";
	}
}
