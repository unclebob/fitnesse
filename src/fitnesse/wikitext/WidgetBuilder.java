// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import fitnesse.wikitext.widgets.*;
import java.lang.reflect.*;
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

	public WidgetBuilder(Class[] widgetClasses)
	{
		this.widgetClasses = widgetClasses;
		widgetPattern = buildCompositeWidgetPattern();
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
}
