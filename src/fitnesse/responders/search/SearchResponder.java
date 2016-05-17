// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import java.util.regex.Pattern;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.RegularExpressionWikiPageFinder;
import fitnesse.wiki.search.TitleWikiPageFinder;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.LITERAL;

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
    return (request.getInput("searchType") == null) ? "Search Form" : getSearchType() + " Search Results for '" + getSearchString() + "'";
  }

  @Override
  protected PageFinder getPageFinder(TraversalListener<WikiPage> observer) {
    String searchString = getSearchString();
    if (!"".equals(searchString)) {
      String searchType = getSearchType();
      if ("Title".equals(searchType))
        return new TitleWikiPageFinder(searchString, observer);
      else {
        Pattern regularExpression = Pattern.compile(searchString, CASE_INSENSITIVE + LITERAL);
        return new RegularExpressionWikiPageFinder(regularExpression, observer);
      }
    }
    return null;
  }

  @Override
  protected boolean shouldRespondWith404() {
    return false;
  }

}
