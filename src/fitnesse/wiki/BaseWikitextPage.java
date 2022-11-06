// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.util.Clock;
import fitnesse.wiki.fs.WikiPageProperties;
import fitnesse.wikitext.*;

import static fitnesse.wiki.PageType.STATIC;

/**
 * This class adds support for FitNesse wiki text ({@link fitnesse.wikitext.parser.Parser}).
 */
public abstract class BaseWikitextPage extends BaseWikiPage implements WikitextPage {

  private final VariableSource variableSource;
  private ParsingPage parsingPage;
  private SyntaxTree syntaxTree;

  protected BaseWikitextPage(String name, VariableSource variableSource) {
    this(name, null, variableSource);
  }

  protected BaseWikitextPage(String name, WikiPage parent) {
    this(name, parent, parent instanceof BaseWikitextPage ? ((BaseWikitextPage) parent).variableSource : null);
  }

  protected BaseWikitextPage(String name, WikiPage parent, VariableSource variableSource) {
    super(name, parent);
    this.variableSource = variableSource;
  }

  protected VariableSource getVariableSource() {
    return variableSource;
  }

  @Override
  public String getVariable(String name) {
    return getSyntaxTree().findVariable(name)
      .map(value -> MarkUpSystem.make().variableValueToHtml(parsingPage, value))
      .orElse(null);
  }

  @Override
  public String getHtml() {
    parse();
    return syntaxTree.translateToHtml();
  }

  @Override
  public SyntaxTree getSyntaxTree() {
    parse();
    return syntaxTree;
  }

  private void parse() {
    if (syntaxTree == null) {
      parsingPage = makeParsingPage(this);
      String content = getData().getContent();
      syntaxTree = MarkUpSystem.make(content).parse(parsingPage, content);
    }
  }

  protected void resetCache() {
    parsingPage = null;
    syntaxTree = null;
  }

  public static ParsingPage makeParsingPage(BaseWikitextPage page) {
    return new ParsingPage(page, page.variableSource);
  }

  public WikiPageProperty defaultPageProperties() {
    WikiPageProperties properties = new WikiPageProperties();
    properties.set(WikiPageProperty.EDIT);
    properties.set(WikiPageProperty.PROPERTIES);
    properties.set(WikiPageProperty.REFACTOR);
    properties.set(WikiPageProperty.WHERE_USED);
    properties.set(WikiPageProperty.RECENT_CHANGES);
    properties.set(WikiPageProperty.FILES);
    properties.set(WikiPageProperty.VERSIONS);
    properties.set(WikiPageProperty.SEARCH);
    properties.setLastModificationTime(Clock.currentDate());

    PageType pageType = PageType.getPageTypeForPageName(getName());

    if (STATIC.equals(pageType))
      return properties;

    properties.set(pageType.toString());
    return properties;
  }
}
