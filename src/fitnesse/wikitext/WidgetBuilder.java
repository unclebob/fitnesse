// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import fitnesse.wikitext.widgets.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.*;

public class WidgetBuilder
{
	public static final Class[] htmlWidgetClasses = new Class[]{
		CommentWidget.class,
		LiteralWidget.class,
		WikiWordWidget.class,
		BoldWidget.class,
		ItalicWidget.class,
		PreformattedWidget.class,
		HruleWidget.class,
		HeaderWidget.class,
		CenterWidget.class,
		NoteWidget.class,
		TableWidget.class,
		ListWidget.class,
		ClasspathWidget.class,
		LineBreakWidget.class,
		ImageWidget.class,
		LinkWidget.class,
		TOCWidget.class,
		AliasLinkWidget.class,
		VirtualWikiWidget.class,
		StrikeWidget.class,
		LastModifiedWidget.class,
		FixtureWidget.class,
		XRefWidget.class,
		MetaWidget.class,
		EmailWidget.class,
		AnchorDeclarationWidget.class,
		AnchorMarkerWidget.class,
		CollapsableWidget.class,
		IncludeWidget.class,
		VariableDefinitionWidget.class,
		VariableWidget.class
	};

	public static WidgetBuilder htmlWidgetBuilder = new WidgetBuilder(htmlWidgetClasses);
	public static WidgetBuilder literalAndVariableWidgetBuilder = new WidgetBuilder(new Class[]{LiteralWidget.class, VariableWidget.class});
	public static WidgetBuilder variableWidgetBuilder = new WidgetBuilder(new Class[]{VariableWidget.class});

	private Class[] widgetClasses;
	private Pattern widgetPattern;
	private WidgetData[] widgetDataArray;

	private List<WidgetInterceptor> interceptors = new LinkedList<WidgetInterceptor>();
	private final ReentrantLock widgetDataArraylock = new ReentrantLock();

	public WidgetBuilder(Class[] widgetClasses)
	{
		this.widgetClasses = widgetClasses;
		widgetPattern = buildCompositeWidgetPattern();

		widgetDataArray = buildWidgetDataArray();
	}

	private Pattern buildCompositeWidgetPattern()
	{
		StringBuffer pattern = new StringBuffer();
		for(int i = 0; i < widgetClasses.length; i++)
		{
			Class widgetClass = widgetClasses[i];
			String regexp = getRegexpFromWidgetClass(widgetClass);
			pattern.append("(").append(regexp).append(")");
			if(i != (widgetClasses.length - 1))
				pattern.append("|");
		}
		return Pattern.compile(pattern.toString(), Pattern.DOTALL | Pattern.MULTILINE);
	}

	private static String getRegexpFromWidgetClass(Class widgetClass)
	{
		String regexp = null;
		try
		{
			Field f = widgetClass.getField("REGEXP");
			regexp = (String) f.get(widgetClass);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return regexp;
	}

	public void addChildWidgets2(String value, ParentWidget parent) throws Exception
	{
		Matcher matcher = getWidgetPattern().matcher(value);

		if(matcher.find())
		{
			String preString = value.substring(0, matcher.start());
			if(!"".equals(preString))
				new TextWidget(parent, preString);
			makeWidget(parent, matcher);
			String postString = value.substring(matcher.end());
			if(!postString.equals(""))
				addChildWidgets(postString, parent);
		}
		else
			new TextWidget(parent, value);
	}

	public WikiWidget makeWidget(ParentWidget parent, Matcher matcher) throws Exception
	{
		int group = getGroupMatched(matcher);
		Class widgetClass = widgetClasses[group - 1];
		return constructWidget(widgetClass, parent, matcher.group());
	}

	private WikiWidget constructWidget(Class widgetClass, ParentWidget parent, String text) throws Exception
	{
		try
		{
			Constructor widgetConstructor = widgetClass.getConstructor(new Class[]{ParentWidget.class, String.class});
			WikiWidget widget = (WikiWidget) widgetConstructor.newInstance(new Object[]{parent, text});
			for(WidgetInterceptor i : interceptors)
			{
				i.intercept(widget);
			}
			return widget;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Exception exception = new Exception("Widget Construction failed for " + widgetClass.getName() + "\n" + e.getMessage());
			exception.setStackTrace(e.getStackTrace());
			throw exception;
		}
	}

	public int getGroupMatched(Matcher matcher)
	{
		for(int i = 1; i <= matcher.groupCount(); i++)
		{
			if(matcher.group(i) != null)
				return i;
		}
		return -1;
	}

	public Pattern getWidgetPattern()
	{
		return widgetPattern;
	}

	private WidgetData[] buildWidgetDataArray()
	{
		WidgetData[] widgetDataArray = new WidgetData[widgetClasses.length];
		for(int i = 0; i < widgetClasses.length; i++)
		{
			Class widgetClass = widgetClasses[i];
			widgetDataArray[i] = new WidgetData(widgetClass);
		}
		return widgetDataArray;
	}

	public void addChildWidgets(String value, ParentWidget parent) throws Exception
	{
		widgetDataArraylock.lock();
		WidgetData firstMatch = findFirstMatch(value);
		try
		{
			if(firstMatch != null)
			{
				Matcher match = firstMatch.match;
				String preString = value.substring(0, match.start());
				if(!"".equals(preString))
					new TextWidget(parent, preString);
				constructWidget(firstMatch.widgetClass, parent, match.group());
				String postString = value.substring(match.end());
				if(!postString.equals(""))
					addChildWidgets(postString, parent);
			}
			else
				new TextWidget(parent, value);
		}
		finally
		{
			widgetDataArraylock.unlock();
		}
	}

	private WidgetData findFirstMatch(String value)
	{
		resetWidgetDataList();

		WidgetData firstMatch = null;
		for(int i = 0; i < widgetDataArray.length; i++)
		{
			WidgetData widgetData = widgetDataArray[i];
			Matcher match = widgetData.pattern.matcher(value);
			if(match.find())
			{
				widgetData.match = match;
				if(firstMatch == null)
					firstMatch = widgetData;
				else if(match.start() < firstMatch.match.start())
					firstMatch = widgetData;
			}
		}
		return firstMatch;
	}

	private void resetWidgetDataList()
	{
		for(int i = 0; i < widgetDataArray.length; i++)
			widgetDataArray[i].match = null;
	}

	public void addInterceptor(WidgetInterceptor interceptor)
	{
		interceptors.add(interceptor);
	}

	static class WidgetData
	{
		public Class widgetClass;
		public Pattern pattern;
		public Matcher match;

		public WidgetData(Class widgetClass)
		{
			this.widgetClass = widgetClass;
			pattern = Pattern.compile(getRegexpFromWidgetClass(widgetClass), Pattern.DOTALL | Pattern.MULTILINE);
		}
	}
}
