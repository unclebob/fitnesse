// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.widgets.AliasLinkWidget;
import fitnesse.wikitext.widgets.AnchorDeclarationWidget;
import fitnesse.wikitext.widgets.AnchorMarkerWidget;
import fitnesse.wikitext.widgets.BoldWidget;
import fitnesse.wikitext.widgets.CenterWidget;
import fitnesse.wikitext.widgets.ClasspathWidget;
import fitnesse.wikitext.widgets.CollapsableWidget;
import fitnesse.wikitext.widgets.CommentWidget;
import fitnesse.wikitext.widgets.EmailWidget;
import fitnesse.wikitext.widgets.EvaluatorWidget;
import fitnesse.wikitext.widgets.HeaderWidget;
import fitnesse.wikitext.widgets.HruleWidget;
import fitnesse.wikitext.widgets.ImageWidget;
import fitnesse.wikitext.widgets.IncludeWidget;
import fitnesse.wikitext.widgets.ItalicWidget;
import fitnesse.wikitext.widgets.LastModifiedWidget;
import fitnesse.wikitext.widgets.LinkWidget;
import fitnesse.wikitext.widgets.ListWidget;
import fitnesse.wikitext.widgets.LiteralWidget;
import fitnesse.wikitext.widgets.MetaWidget;
import fitnesse.wikitext.widgets.NoteWidget;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.PreformattedWidget;
import fitnesse.wikitext.widgets.StrikeWidget;
import fitnesse.wikitext.widgets.StyleWidget;
import fitnesse.wikitext.widgets.TOCWidget;
import fitnesse.wikitext.widgets.TableWidget;
import fitnesse.wikitext.widgets.TextWidget;
import fitnesse.wikitext.widgets.TodayWidget;
import fitnesse.wikitext.widgets.VariableDefinitionWidget;
import fitnesse.wikitext.widgets.VariableWidget;
import fitnesse.wikitext.widgets.VirtualWikiWidget;
import fitnesse.wikitext.widgets.WikiWordWidget;
import fitnesse.wikitext.widgets.XRefWidget;

@SuppressWarnings("unchecked")
public class WidgetBuilder {
  public static WidgetBuilder htmlWidgetBuilder = new WidgetBuilder(
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
    StyleWidget.ParenFormat.class,
    StyleWidget.BraceFormat.class,
    StyleWidget.BracketFormat.class,
    TableWidget.class,
    ListWidget.class,
    ClasspathWidget.class,
    ImageWidget.class,
    LinkWidget.class,
    TOCWidget.class,
    AliasLinkWidget.class,
    VirtualWikiWidget.class,
    StrikeWidget.class,
    LastModifiedWidget.class,
    TodayWidget.class,
    XRefWidget.class,
    MetaWidget.class,
    EmailWidget.class,
    AnchorDeclarationWidget.class,
    AnchorMarkerWidget.class,
    CollapsableWidget.class,
    IncludeWidget.class,
    VariableDefinitionWidget.class,
    EvaluatorWidget.class,
    VariableWidget.class
  );

  public static WidgetBuilder literalVariableEvaluatorWidgetBuilder = new WidgetBuilder(
    LiteralWidget.class,
    EvaluatorWidget.class,
    VariableWidget.class
  );

  public static WidgetBuilder variableEvaluatorWidgetBuilder = new WidgetBuilder(
    EvaluatorWidget.class,
    VariableWidget.class
  );

  private List<WidgetData> widgetData = new ArrayList<WidgetData>();

  private List<WidgetInterceptor> interceptors = new LinkedList<WidgetInterceptor>();
  private final ReentrantLock widgetDataArraylock = new ReentrantLock();

  public WidgetBuilder() {
  }
  
  public WidgetBuilder(Class<? extends WikiWidget>... widgetClasses) {
    for (Class<? extends WikiWidget> widgetClass : widgetClasses) {
      addWidgetClass(widgetClass);
    }
  }

  public final void addWidgetClass(Class<?> widgetClass) {
    widgetData.add(new WidgetData(widgetClass));
  }

  private WikiWidget constructWidget(Class<?> widgetClass, ParentWidget parent, String text) {
    try {
      Constructor<?> widgetConstructor = widgetClass.getConstructor(ParentWidget.class, String.class);
      WikiWidget widget = (WikiWidget) widgetConstructor.newInstance(parent, text);
      for (WidgetInterceptor i : interceptors) {
        i.intercept(widget);
      }
      return widget;
    }
    catch (Exception e) {
      RuntimeException exception = new RuntimeException("Widget Construction failed for " + 
        widgetClass.getName() + "\n" + e.getMessage());
      exception.setStackTrace(e.getStackTrace());
      throw exception;
    }
  }

  public void addChildWidgets(String value, ParentWidget parent) {
    addChildWidgets(value, parent, true);
  }

  public void addChildWidgets(String value, ParentWidget parent, boolean includeTextWidgets) {
    widgetDataArraylock.lock();
    WidgetData firstMatch = findFirstMatch(value);
    try {
      if (firstMatch != null) {
        Matcher match = firstMatch.match;
        String preString = value.substring(0, match.start());
        if (!"".equals(preString) && includeTextWidgets)
          new TextWidget(parent, preString);
        constructWidget(firstMatch.widgetClass, parent, match.group());
        String postString = value.substring(match.end());
        if (!postString.equals(""))
          addChildWidgets(postString, parent, includeTextWidgets);
      } else if (includeTextWidgets)
        new TextWidget(parent, value);
    }
    finally {
      widgetDataArraylock.unlock();
    }
  }

  public Class<?> findWidgetClassMatching(String value) {
    WidgetData firstMatch = findFirstMatch(value);
    return firstMatch == null ? null : firstMatch.widgetClass;
  }

  private WidgetData findFirstMatch(String value) {
    resetWidgetDataList();

    WidgetData firstMatch = null;
    for (WidgetData widgetData : this.widgetData) {
      Matcher match = widgetData.pattern.matcher(value);
      if (match.find()) {
        widgetData.match = match;
        if (firstMatch == null)
          firstMatch = widgetData;
        else if (match.start() < firstMatch.match.start())
          firstMatch = widgetData;
      }
    }
    return firstMatch;
  }

  private void resetWidgetDataList() {
    for (WidgetData widgetData : this.widgetData)
      widgetData.match = null;
  }

  public void addInterceptor(WidgetInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  static class WidgetData {
    public Class<?> widgetClass;
    public Pattern pattern;
    public Matcher match;

    public WidgetData(Class<?> widgetClass) {
      this.widgetClass = widgetClass;
      pattern = Pattern.compile(getRegexpFromWidgetClass(widgetClass), Pattern.DOTALL | Pattern.MULTILINE);
    }

    private static String getRegexpFromWidgetClass(Class<?> widgetClass) {
      String regexp = null;
      try {
        Field f = widgetClass.getField("REGEXP");
        regexp = (String) f.get(widgetClass);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return regexp;
    }
  }
}
