/**
 * Copyright AdScale GmbH, Germany, 2009
 */
package fitnesse.responders.run;

import util.StringUtil;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SuiteFilter {
  final private SuiteTagMatcher notMatchTags;
  final private SuiteTagMatcher matchTags;
  final private String startWithTest;
  
  public static SuiteFilter NO_MATCHING = new SuiteFilter(null, null, null) {
    public boolean isMatchingTest(WikiPage testPage) throws Exception {
      return false;
    }
  };
  
  public static SuiteFilter MATCH_ALL = new SuiteFilter(null, null, null);

  
  SuiteFilter(String matchingTags, String mustNotMatchTags, String startWithTest) {
    this.startWithTest = (!"".equals(startWithTest)) ? startWithTest : null;
    
    matchTags = new SuiteTagMatcher(matchingTags, true);
    notMatchTags = new SuiteTagMatcher(mustNotMatchTags, false);
  }
  
  public boolean isMatchingTest(WikiPage testPage) throws Exception {
    PageData data = testPage.getData();
    boolean pruned = data.hasAttribute(PageData.PropertyPRUNE);
    boolean isTest = data.hasAttribute("Test");
    return !pruned && 
           isTest && 
           matchTags.matches(testPage) &&
           !notMatchTags.matches(testPage) && 
           afterStartingTest(testPage);
  }
  
  private boolean afterStartingTest(WikiPage testPage) throws Exception {
    if (startWithTest == null) {
      return true;
    }
    PageCrawler crawler = testPage.getPageCrawler();
    WikiPagePath pageName = crawler.getFullPath(testPage);
    return (pageName.toString().compareTo(startWithTest) >= 0);
  }

  public boolean hasMatchingTests() {
    return (this != NO_MATCHING);
  }

  public SuiteFilter getFilterForTestsInSuite(WikiPage suitePage) throws Exception {
    if (suitePage.getData().hasAttribute(PageData.PropertyPRUNE)) {
      return NO_MATCHING;
    }
    
    PageData pageData = suitePage.getData();
    if (pageData.hasAttribute("Suite") && matchTags.isFiltering() && matchTags.matches(suitePage)) {
      return new SuiteFilter(null, notMatchTags.tagString, startWithTest).getFilterForTestsInSuite(suitePage);
    }
    
    if (notMatchTags.matches(suitePage)) {
      return NO_MATCHING;
    }

    return this;
  }
  
  
  @Override
  public String toString() {
    List<String> criterias = new LinkedList<String>();
    
    if (matchTags.isFiltering()) {
      criterias.add("matches '" + matchTags.tagString + "'");
    }

    if (notMatchTags.isFiltering()) {
      criterias.add("doesn't match '" + notMatchTags.tagString + "'");
    }

    if (startWithTest != null) {
      criterias.add("starts with test '" + startWithTest + "'");
    }
    
    return StringUtil.join(criterias, " & ");
  }
  
  private class SuiteTagMatcher {
    private static final String LIST_SEPARATOR = "\\s*,\\s*";
    final private List<String> tags;
    final String tagString;
    final private boolean matchIfNoTags;
    
    public SuiteTagMatcher(String suiteTags, boolean matchIfNoTags) {
      tagString = suiteTags;
      if (suiteTags != null) {
        tags = new LinkedList<String>(Arrays.asList(suiteTags.split(LIST_SEPARATOR)));
      }
      else {
        tags = null;
      }
      this.matchIfNoTags = matchIfNoTags;
    }
    
    boolean isFiltering() {
      return (tags != null);
    }
    
    boolean matches(WikiPage wikiPage) {
      return (tags == null) ? matchIfNoTags : testMatchesQuery(wikiPage);
    }

    private boolean testMatchesQuery(WikiPage wikiPage) {
      String testTagString = getTestTags(wikiPage);
      return (testTagString != null && testTagsMatchQueryTags(testTagString));
    }
    
    private String getTestTags(WikiPage context) {
      try {
        return context.getData().getAttribute(PageData.PropertySUITES);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }

    private boolean testTagsMatchQueryTags(String testTagString) {
      String testTags[] = testTagString.trim().split(LIST_SEPARATOR);
      for (String testTag : testTags) {
        for (String queryTag : tags) {
          if (testTag.equalsIgnoreCase(queryTag)) {
            return true;
          }
        }
      }
      return false;
    }
  }
}
