/**
 * Copyright AdScale GmbH, Germany, 2009
 */
package fitnesse.responders.run;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import util.StringUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SuiteFilter {
  final private List<String> suiteTags = new LinkedList<String>();
  final private String startWithTest;
  
  private static SuiteFilter NO_MATCHING = new SuiteFilter(null, null) {
    public boolean isMatchingTest(WikiPage testPage) throws Exception {
      return false;
    }
  };
  
  SuiteFilter(String matchingTags, String startWithTest) {
    this.startWithTest = (!"".equals(startWithTest)) ? startWithTest : null;
    
    if (matchingTags != null) {
      suiteTags.addAll(Arrays.asList(matchingTags.split("\\s*,\\s*")));
    }
  }
  
  public boolean isMatchingTest(WikiPage testPage) throws Exception {
    PageData data = testPage.getData();
    boolean pruned = data.hasAttribute(PageData.PropertyPRUNE);
    boolean test = data.hasAttribute("Test");
    return !pruned && test && belongsToSuite(testPage) && afterStartingTest(testPage);
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
    if ((suiteTags.size() > 0) && pageData.hasAttribute("Suite") && belongsToSuite(suitePage)) {
      return new SuiteFilter(null, startWithTest).getFilterForTestsInSuite(suitePage);
    }

    return this;
  }
  
  private boolean belongsToSuite(WikiPage wikiPage) {
    return (suiteTags.size() == 0) || testMatchesQuery(wikiPage);
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
    String testTags[] = testTagString.trim().split("\\s*,\\s*");
    for (String testTag : testTags) {
      for (String queryTag : suiteTags) {
        if (testTag.equalsIgnoreCase(queryTag)) {
          return true;
        }
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuffer description = new StringBuffer();
    
    if (suiteTags.size() > 0) {
      description.append("has suite filter '").append(StringUtil.join(suiteTags, ", ")).append("'");
    }

    if (startWithTest != null) {
      if (description.length() > 0) {
        description.append(" & ");
      }
      description.append("starts with test '").append(startWithTest).append("'");
    }
    
    return description.toString();
  }
}
