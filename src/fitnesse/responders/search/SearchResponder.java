// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import java.util.regex.Pattern;

import fitnesse.components.TraversalListener;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.search.MethodWikiPageFinder;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.RegularExpressionWikiPageFinder;
import fitnesse.wiki.search.TitleWikiPageFinder;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.LITERAL;
import static java.util.regex.Pattern.UNICODE_CASE;

public class SearchResponder extends ResultResponder {

  private String getSearchString() {
    String searchString = request.getInput("searchString");
    return searchString == null ? "" : searchString;
  }

  private String getSearchType() {
    String searchType = request.getInput("searchType");

    if (searchType == null || searchType.toLowerCase().contains("title"))
      return "Title";
    else
      return "Content";
  }


  protected String getPageFooterInfo(int hits) {
    return "Found " + hits + " results for your search.";
  }

  @Override
  protected String getTemplate() {
    return "searchForm";
  }

  @Override
  protected String getTitle() {
    return (request.getInput("searchType") == null)
      ? "Search Form"
      : getSearchType() + " Search Results for '" + HtmlUtil.escapeHTML(getSearchString()) + "'";
  }

  @Override
  protected PageFinder getPageFinder(TraversalListener<WikiPage> observer) {
    String searchString = getSearchString();
    if (!"".equals(searchString)) {
      String searchType = getSearchType();
      if ("Title".equals(searchType)) {
        return useWildcards()
          ? new TitleWikiPageFinder(wildcardPattern(searchString), observer)
          : new TitleWikiPageFinder(searchString, observer);
      } else {
        if (isMethodSearch()) {
          return new MethodWikiPageFinder(searchString, observer);
        } else {
          Pattern regularExpression = useWildcards()
            ? wildcardPattern(searchString)
            : Pattern.compile(searchString, CASE_INSENSITIVE + LITERAL);
          return new RegularExpressionWikiPageFinder(regularExpression, observer);
        }
      }
    }
    return null;
  }

  private boolean isMethodSearch() {
    return request.hasInput("isMethodSearch");
  }

  private boolean useWildcards() {
    return request.hasInput("useWildcards");
  }

  /**
   * @param searchString text to search for, where * matches any characters (within a line)
   * @return case insensitive pattern matching the search string
   */
  static Pattern wildcardPattern(String searchString) {
    StringBuilder regularExpression = new StringBuilder();
    String[] parts = searchString.split("\\*", -1);
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        regularExpression.append(".*");
      }
      if (!parts[i].isEmpty()) {
        regularExpression.append(Pattern.quote(parts[i]));
      }
    }
    return Pattern.compile(regularExpression.toString(), CASE_INSENSITIVE | UNICODE_CASE);
  }

  @Override
  protected boolean shouldRespondWith404() {
    return false;
  }

}
