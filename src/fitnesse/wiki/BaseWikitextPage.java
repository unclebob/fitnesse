// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.util.Clock;
import fitnesse.wiki.fs.WikiPageProperties;
import fitnesse.wikitext.*;
import fitnesse.wikitext.parser.Maybe;

import java.util.Optional;

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
      syntaxTree = MarkUpSystem.make().parse(parsingPage, getData().getContent());
    }
  }

  protected void resetCache() {
    parsingPage = null;
    syntaxTree = null;
  }

  public static ParsingPage makeParsingPage(BaseWikitextPage page) {
    ParsingPage.Cache cache = new ParsingPage.Cache();

    VariableSource compositeVariableSource = new CompositeVariableSource(
            new ApplicationVariableSource(page.variableSource),
            new PageVariableSource(page),
            new UserVariableSource(page.variableSource),
            cache,
            new ParentPageVariableSource(page),
            page.variableSource);
    return new ParsingPage(new WikiSourcePage(page), compositeVariableSource, cache);
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

  public static class UserVariableSource implements VariableSource {

    private final VariableSource variableSource;

    public UserVariableSource(VariableSource variableSource) {
      this.variableSource = variableSource;
    }

    @Override
    public Optional<String> findVariable(String name) {
      if(variableSource instanceof UrlPathVariableSource){
        Maybe<String> result = ((UrlPathVariableSource) variableSource).findUrlVariable(name);
        if (!result.isNothing()) return Optional.of(result.getValue());
      }
      return Optional.empty();
    }
  }

  public static class ParentPageVariableSource implements VariableSource {
    private final WikiPage page;

    public ParentPageVariableSource(WikiPage page) {

      this.page = page;
    }

    @Override
    public Optional<String> findVariable(String name) {
      if (page.isRoot()) {
        return Optional.empty();
      }
      WikiPage parentPage = page.getParent();
      if (parentPage instanceof WikitextPage) {
        return ((WikitextPage) parentPage).getSyntaxTree().findVariable(name);
      } else {
        return Optional.ofNullable(parentPage.getVariable(name));
      }
    }
  }
}
