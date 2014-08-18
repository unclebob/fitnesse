// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.ParsedPage;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wikitext.parser.WikiSourcePage;
import util.Maybe;

public abstract class BaseWikiPage implements WikiPage, WikitextPage {
  private static final long serialVersionUID = 1L;

  private final String name;
  private final BaseWikiPage parent;
  private final VariableSource variableSource;
  private ParsedPage parsedPage;

  protected BaseWikiPage(String name, VariableSource variableSource) {
    this(name, null, variableSource);
  }

  protected BaseWikiPage(String name, BaseWikiPage parent) {
    this(name, parent, parent.variableSource);
  }

  protected BaseWikiPage(String name, BaseWikiPage parent, VariableSource variableSource) {
    this.name = name;
    this.parent = parent;
    this.variableSource = variableSource;
  }
  public String getName() {
    return name;
  }

  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl(this);
  }

  public BaseWikiPage getParent() {
    return parent == null ? this : parent;
  }


  public boolean isRoot() {
    return parent == null || parent == this;
  }

  protected VariableSource getVariableSource() {
    return variableSource;
  }


  @Override
  public String getVariable(String name) {
    ParsingPage parsingPage = getParsingPage();
    Maybe<String> variable = parsingPage.findVariable(name);
    if (variable.isNothing()) return null;

    Parser parser = Parser.make(parsingPage, "", SymbolProvider.variableDefinitionSymbolProvider);
    return new HtmlTranslator(null, parsingPage).translate(parser.parseWithParent(variable.getValue(), null));
  }

  protected final ParsedPage getParsedPage() {
    if (parsedPage == null) {
      parsedPage = new ParsedPage(new ParsingPage(new WikiSourcePage(this), getVariableSource()), getData().getContent());
    }
    return parsedPage;
  }


  @Override
  public String getHtml() {
    return getParsedPage().toHtml();
  }

  protected void resetParsedPage() {
    parsedPage = null;
  }

  @Override
  public ParsingPage getParsingPage() {
    return getParsedPage().getParsingPage();
  }

  @Override
  public Symbol getSyntaxTree() {
    return getParsedPage().getSyntaxTree();
  }

  public String toString() {
    return this.getClass().getName() + ": " + name;
  }

  public int compareTo(Object o) {
    try {
      return getPageCrawler().getFullPath().compareTo(((WikiPage) o).getPageCrawler().getFullPath());
    }
    catch (Exception e) {
      return 0;
    }
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof WikiPage))
      return false;
    try {
      return getPageCrawler().getFullPath().equals(((WikiPage) o).getPageCrawler().getFullPath());
    }
    catch (Exception e) {
      return false;
    }
  }

  public int hashCode() {
    try {
      return getPageCrawler().getFullPath().hashCode();
    }
    catch (Exception e) {
      return 0;
    }
  }
}
