// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PagePointer;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;

public class WidgetRoot extends ParentWidget {
  private Map<String, String> variables = new HashMap<String, String>();
  private WidgetBuilder builder;
  private WikiPage page;
  private boolean doEscaping = true;
  private List<String> literals = new LinkedList<String>();
  private boolean isGatheringInfo = false;

  //Constructor for IncludeWidget support (alias locale & scope)
  public WidgetRoot(WikiPage aliasPage, ParentWidget imposterWidget) throws Exception {
    super(imposterWidget, /*is alias=*/ true);
    WidgetRoot aliasRoot = imposterWidget.getRoot();

    this.builder = imposterWidget.getBuilder();
    this.variables = aliasRoot.variables;
    this.doEscaping = aliasRoot.doEscaping;
    this.literals = aliasRoot.literals;
    this.isGatheringInfo = aliasRoot.isGatheringInfo;
    this.page = aliasPage;
  }

  public WidgetRoot getRoot() {
    return this;
  }

  public boolean isGatheringInfo() {
    return isGatheringInfo;
  }

  public WidgetRoot(WikiPage page) throws Exception {
    this("", page, WidgetBuilder.htmlWidgetBuilder);
  }

  public WidgetRoot(String value, WikiPage page) throws Exception {
    this(value, page, WidgetBuilder.htmlWidgetBuilder);
  }

  public WidgetRoot(String value, WikiPage page, WidgetBuilder builder) throws Exception {
    this(value, page, builder, false);
  }

  public WidgetRoot(String value, WikiPage page, WidgetBuilder builder, boolean isGathering) throws Exception {
    super(null);
    this.page = page;
    this.builder = builder;
    this.isGatheringInfo = isGathering;
    if (value != null)
      buildWidgets(value);
  }

  public WidgetRoot(PagePointer pagePointer) throws Exception {
    this("", pagePointer, WidgetBuilder.htmlWidgetBuilder);
  }

  public WidgetRoot(String value, PagePointer pagePointer) throws Exception {
    this(value, pagePointer, WidgetBuilder.htmlWidgetBuilder);
  }

  public WidgetRoot(String value, PagePointer pagePointer, WidgetBuilder builder) throws Exception {
    super(null);
    this.page = pagePointer.getPage();
    this.builder = builder;
    if (value != null)
      buildWidgets(value);
  }

  public WidgetBuilder getBuilder() {
    return builder;
  }

  protected void buildWidgets(String value) throws Exception {
    String nonLiteralContent = processLiterals(stripTrialingWhiteSpaceInLines(value));
    addChildWidgets(nonLiteralContent);
  }

  public String render() throws Exception {
    return childHtml();
  }

  public String getVariable(String key) throws Exception {
    String value = (String) variables.get(key);

    if (key.equals("PAGE_NAME"))
      value = page.getName();
    else if (key.equals("PAGE_PATH"))
      value = getWikiPage().getPageCrawler().getFullPath(page).parentPath().toString();
    else if (key.equals("FITNESSE_PORT"))
      value = Integer.toString(FitNesseContext.globalContext.port);

    WikiPage page = getWikiPage();
    while (value == null && !page.getPageCrawler().isRoot(page)) {
      page = page.getParentForVariables(); // follow parents for variables
      // Gain access to page data to set parent's literal list
      PageData pageData = page.getData();
      pageData.setLiterals(this.getLiterals());
      value = pageData.getVariable(key);
    }
    if (value == null) {
      value = System.getenv(key);
    }
    if (value == null) {
      value = System.getProperty(key);
    }
    return value;
  }

  public void addVariable(String key, String value) {
    variables.put(key, value);
  }

  public int defineLiteral(String literal) {
    int literalNumber = literals.size();
    literals.add(literal);
    return literalNumber;
  }

  public String getLiteral(int literalNumber) {
    if (literalNumber >= literals.size())
      return "literal(" + literalNumber + ") not found.";
    return (String) literals.get(literalNumber);
  }

  public WikiPage getWikiPage() {
    return page;
  }

  public void setEscaping(boolean value) {
    doEscaping = value;
  }

  public boolean doEscaping() {
    return doEscaping;
  }

  public List<String> getLiterals() {
    return literals;
  }

  public void setLiterals(List<String> literals) {
    this.literals = literals;
  }

  public String asWikiText() throws Exception {
    return childWikiText();
  }
}

