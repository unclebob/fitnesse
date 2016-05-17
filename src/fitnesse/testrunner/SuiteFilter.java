/**
 * Copyright AdScale GmbH, Germany, 2009
 */
package fitnesse.testrunner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.trim;

public class SuiteFilter {
  private static final Logger LOG = Logger.getLogger(SuiteFilter.class.getName());

  private final SuiteTagMatcher notMatchTags;
  private final SuiteTagMatcher matchTags;
  private final String startWithTest;

  public static final SuiteFilter NO_MATCHING = new SuiteFilter(null, null, null, null) {
    @Override
    public boolean isMatchingTest(WikiPage testPage) {
      return false;
    }
  };

  public static final SuiteFilter MATCH_ALL = new SuiteFilter(null, null, null, null);

  public SuiteFilter(String orTags, String mustNotMatchTags, String andTags, String startWithTest) {
    this.startWithTest = (!"".equals(startWithTest)) ? startWithTest : null;
    if(andTags != null) {
      matchTags = new SuiteTagMatcher(andTags, true, true);
    } else {
      matchTags = new SuiteTagMatcher(orTags, true, false);
    }
    notMatchTags = new SuiteTagMatcher(mustNotMatchTags, false, false);
  }

  public SuiteFilter(String suiteFilter, String excludeSuiteFilter) {
    matchTags = new SuiteTagMatcher(suiteFilter, true, false);
    notMatchTags = new SuiteTagMatcher(excludeSuiteFilter, false, false);
    startWithTest = null;
  }

  public boolean isMatchingTest(WikiPage testPage) {
    PageData data = testPage.getData();
    boolean isTest = data.hasAttribute(PageType.TEST.toString());
    return isTest &&
           matchTags.matches(testPage) &&
           !notMatchTags.matches(testPage) &&
           afterStartingTest(testPage);
  }

  private boolean afterStartingTest(WikiPage testPage) {
    if (startWithTest == null) {
      return true;
    }
    PageCrawler crawler = testPage.getPageCrawler();
    WikiPagePath pageName = crawler.getFullPath();
    return (pageName.toString().compareTo(startWithTest) >= 0);
  }

  public boolean hasMatchingTests() {
    return (this != NO_MATCHING);
  }

  public SuiteFilter getFilterForTestsInSuite(WikiPage suitePage) {
    PageData pageData = suitePage.getData();
    if (pageData.hasAttribute(PageType.SUITE.toString()) && matchTags.isFiltering() && matchTags.matches(suitePage)) {
      return new SuiteFilter(null, notMatchTags.tagString, null, startWithTest).getFilterForTestsInSuite(suitePage);
    }

    if (notMatchTags.matches(suitePage)) {
      return NO_MATCHING;
    }

    return this;
  }


  @Override
  public String toString() {
    List<String> criterias = new LinkedList<>();

    if (matchTags.isFiltering()) {
      if(matchTags.andStrategy){
        criterias.add("matches all of '" + matchTags.tagString + "'");
      } else {
        criterias.add("matches '" + matchTags.tagString + "'");
      }
    }

    if (notMatchTags.isFiltering()) {
      criterias.add("doesn't match '" + notMatchTags.tagString + "'");
    }

    if (startWithTest != null) {
      criterias.add("starts with test '" + startWithTest + "'");
    }

    return StringUtils.join(criterias, " & ");
  }

  private class SuiteTagMatcher {
    private static final String LIST_SEPARATOR = "\\s*,\\s*";
    private final List<String> tags;
    final String tagString;
    private final boolean matchIfNoTags;
    private final boolean andStrategy;

    public SuiteTagMatcher(String suiteTags, boolean matchIfNoTags, boolean andStrategy) {
      tagString = suiteTags;
      if (StringUtils.isNotBlank(suiteTags)) {
        tags = Arrays.asList(suiteTags.split(LIST_SEPARATOR));
      }
      else {
        tags = null;
      }
      this.matchIfNoTags = matchIfNoTags;
      this.andStrategy = andStrategy;
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
        LOG.log(Level.WARNING, "Unable to retrieve tags for page " + context, e);
        return null;
      }
    }

    private boolean testTagsMatchQueryTags(String testTagString) {
      String[] testTags = testTagString.trim().split(LIST_SEPARATOR);
      if(andStrategy){
        return checkIfAllQueryTagsExist(testTags);
      } else {
        return checkIfAnyTestTagMatchesAnyQueryTag(testTags);
      }
    }

    private boolean checkIfAllQueryTagsExist(String[] testTags) {
      for (String queryTag : tags) {
        if (!containsTag(testTags, queryTag)) {
          return false;
        }
      }
      return true;
    }

    private boolean checkIfAnyTestTagMatchesAnyQueryTag(String[] testTags) {
      for (String queryTag : tags) {
        if (containsTag(testTags, queryTag)) {
          return true;
        }
      }
      return false;
    }

    private boolean containsTag(String[] testTags, String queryTag) {
      for (String testTag: testTags) {
        if (tagsMatch(queryTag, testTag)) {
          return true;
        }
      }
      return false;
    }

    private boolean tagsMatch(String queryTag, String testTag) {
      return equalsIgnoreCase(trim(testTag), trim(queryTag));
    }
  }
}
