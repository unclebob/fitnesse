// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import fitnesse.wikitext.widgets.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    StandardTableWidget.class,
    PlainTextTableWidget.class,
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
    VariableWidget.class,
    HashWidget.class,
    HelpWidget.class
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

  private List<WidgetMatcher> widgetMatchers = new ArrayList<WidgetMatcher>();

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
    widgetMatchers.add(new WidgetMatcher(widgetClass));
  }

  public void addChildWidgets(String value, ParentWidget parent) {
    addChildWidgets(value, parent, true);
  }

  public void addChildWidgets(String value, ParentWidget parent, boolean includeTextWidgets) {
    widgetDataArraylock.lock();
    WidgetMatch firstMatch = findFirstMatch(value);
    try {
      if (firstMatch != null) {
        String preString = value.substring(0, firstMatch.matchPosition());
        if (!"".equals(preString) && includeTextWidgets) {
          new TextWidget(parent, preString);
        }
        firstMatch.constructWidget(parent, interceptors);
        String postString = value.substring(firstMatch.matchEnd());
        if (!postString.equals("")) {
          addChildWidgets(postString, parent, includeTextWidgets);
        }
      } else if (includeTextWidgets)
        new TextWidget(parent, value);
    }
    finally {
      widgetDataArraylock.unlock();
    }
  }

  public Class<?> findWidgetClassMatching(String value) {
    WidgetMatch firstMatch = findFirstMatch(value);
    return firstMatch == null ? null : firstMatch.getWidgetClass();
  }

  private WidgetMatch findFirstMatch(String value) {
    WidgetMatch firstMatch = null;
    for (WidgetMatcher widgetMatcher : this.widgetMatchers) {
      WidgetMatch widgetMatch = widgetMatcher.matches(value);
      if (widgetMatch != null) {
        if (firstMatch == null)
          firstMatch = widgetMatch;
        else if (widgetMatch.matchIsBefore(firstMatch))
          firstMatch = widgetMatch;
      }
    }
    return firstMatch;
  }

  public void addInterceptor(WidgetInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  private static class WidgetMatcher {
    private Class<?> widgetClass;
    private Pattern pattern;

    public WidgetMatcher(Class<?> widgetClass) {
      this.widgetClass = widgetClass;
      pattern = Pattern.compile(getRegexpFromWidgetClass(widgetClass), Pattern.DOTALL | Pattern.MULTILINE);
    }

    public static String getRegexpFromWidgetClass(Class<?> widgetClass) {
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

    public WidgetMatch matches(String value) {
      Matcher matcher = pattern.matcher(value);
      WidgetMatch match = null;
      if (matcher.find()) {
        match = new WidgetMatch(widgetClass, matcher);
      }
      return match;
    }
  }

  private static class WidgetMatch {
    private Class<?> widgetClass;
    private Matcher match;

    private WidgetMatch(Class<?> widgetClass, Matcher match) {
      this.widgetClass = widgetClass;
      this.match = match;
    }

    String matchingString() {
      return match.group();
    }

    public int matchPosition() {
      return match.start();
    }

    public int matchEnd() {
      return match.end();
    }

    public boolean matchIsBefore(WidgetMatch aMatch) {
      return matchPosition() < aMatch.matchPosition();
    }

    public Class<?> getWidgetClass() {
      return widgetClass;
    }

    private WikiWidget constructWidget(ParentWidget parent, List<WidgetInterceptor> interceptors) {
      try {
        Constructor<?> widgetConstructor = widgetClass.getConstructor(ParentWidget.class, String.class);
        WikiWidget widget = (WikiWidget) widgetConstructor.newInstance(parent, matchingString());
        for (WidgetInterceptor i : interceptors) {
          i.intercept(widget);
        }
        return widget;
      }
      catch (Exception e) {
        System.out.println("text = " + matchingString());
        RuntimeException exception = new RuntimeException("Widget Construction failed for " +
          widgetClass.getName() + "\n" + e.getMessage());
        exception.setStackTrace(e.getStackTrace());
        throw exception;
      }
    }
  }
}
