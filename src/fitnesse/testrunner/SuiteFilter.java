/**
 * Copyright AdScale GmbH, Germany, 2009
 */
package fitnesse.testrunner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import util.StringUtil;

import static java.util.regex.Pattern.DOTALL;

public class SuiteFilter {
  public static final Logger LOG = Logger.getLogger(SuiteFilter.class.getName());

  final private SuiteTagMatcher notMatchTags;
  final private SuiteTagMatcher matchTags;
  final private boolean andStrategy;
  final private String startWithTest;
  final private Pattern contentPattern;

  public static final SuiteFilter NO_MATCHING = new SuiteFilter(null, null, null, null, null) {
    public boolean isMatchingTest(WikiPage testPage) {
      return false;
    }
  };

  public static final SuiteFilter MATCH_ALL = new SuiteFilter(null, null, null, null, null);

  public SuiteFilter(String orTags, String mustNotMatchTags, String andTags, String startWithTest, String contentPatternString) {
    this.startWithTest = (!"".equals(startWithTest)) ? startWithTest : null;
    if (andTags != null) {
      matchTags = new SuiteTagMatcher(andTags, true);
      andStrategy = true;
    } else {
      matchTags = new SuiteTagMatcher(orTags, true);
      andStrategy = false;
    }
    notMatchTags = new SuiteTagMatcher(mustNotMatchTags, false);
    Pattern contentPattern = null;
    if (contentPatternString != null) {
      contentPattern = Pattern.compile(contentPatternString, DOTALL);
    }
    this.contentPattern = contentPattern;
  }

  public SuiteFilter(String suiteFilter, String excludeSuiteFilter) {
    matchTags = new SuiteTagMatcher(suiteFilter, true);
    notMatchTags = new SuiteTagMatcher(excludeSuiteFilter, false);
    andStrategy = false;
    startWithTest = null;
    contentPattern = null;
  }

  public boolean isMatchingTest(WikiPage testPage) {
    PageData data = testPage.getData();
    boolean pruned = data.hasAttribute(PageData.PropertyPRUNE);
    boolean isTest = data.hasAttribute("Test");
    return !pruned &&
      isTest &&
      matchTags.matches(testPage) &&
      !notMatchTags.matches(testPage) &&
      matches(contentPattern, testPage) &&
      afterStartingTest(testPage);
  }

  private boolean matches(Pattern contentPattern, WikiPage testPage) {
    return contentPattern == null || contentPattern.matcher(testPage.getData().getContent()).matches();
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
    if (suitePage.getData().hasAttribute(PageData.PropertyPRUNE)) {
      return NO_MATCHING;
    }

    PageData pageData = suitePage.getData();
    if (pageData.hasAttribute("Suite") && matchTags.isFiltering() && matchTags.matches(suitePage)) {
      return new SuiteFilter(null, notMatchTags.tagString, null, startWithTest, null).getFilterForTestsInSuite(suitePage);
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
      if (andStrategy) {
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
      } else {
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
        LOG.log(Level.WARNING, "Unable to retrieve tags for page " + context, e);
        return null;
      }
    }

    private boolean testTagsMatchQueryTags(String testTagString) {
      String testTags[] = testTagString.trim().split(LIST_SEPARATOR);
      if (andStrategy) {
        return checkIfAllQueryTagsExist(testTags);
      } else {
        return checkIfAnyTestTagMatchesAnyQueryTag(testTags);
      }
    }

    private boolean checkIfAllQueryTagsExist(String[] testTags) {
      List<String> testTagList = Arrays.asList(testTags);
      for (String queryTag : tags) {
        if (!testTagList.contains(queryTag)) {
          return false;
        }
      }
      return true;
    }

    private boolean checkIfAnyTestTagMatchesAnyQueryTag(String[] testTags) {
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
