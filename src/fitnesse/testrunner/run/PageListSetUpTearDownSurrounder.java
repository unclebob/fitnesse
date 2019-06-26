package fitnesse.testrunner.run;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Groups tests that have same SuiteSetUp and SuiteTearDown.
 * Ensure each of these groups are added to the final list of tests to run, directly surrounded by the appropriate
 * SuiteSetUp and -TearDown. This can mean a single SuiteSetUp or -TearDown is added multiple times: once for each
 * group that requires it.
 */
public class PageListSetUpTearDownSurrounder implements PageListSetUpTearDownProcessor {
  private final Map<String, WikiPage> setUpsAndTearDowns = new HashMap<>();

  @Override
  public List<WikiPage> addSuiteSetUpsAndTearDowns(List<WikiPage> pageList) {
    Map<String, List<WikiPage>> pageSetUpTearDownGroups = createPageSetUpTearDownGroups(pageList);
    return reinsertPagesViaSetUpTearDownGroups(pageSetUpTearDownGroups);
  }

  private Map<String, List<WikiPage>> createPageSetUpTearDownGroups(List<WikiPage> pageList) {
    Map<String, List<WikiPage>> pageSetUpTearDownGroups = new LinkedHashMap<>();
    for (WikiPage page : pageList) {
      makeSetUpTearDownPageGroupForPage(page, pageSetUpTearDownGroups);
    }
    return pageSetUpTearDownGroups;
  }

  private void makeSetUpTearDownPageGroupForPage(WikiPage page, Map<String, List<WikiPage>> pageSetUpTearDownGroups) {
    String group = getSetUpTearDownGroup(page);
    pageSetUpTearDownGroups.computeIfAbsent(group, g -> new LinkedList<>()).add(page);
  }

  private String getSetUpTearDownGroup(WikiPage page) {
    String setUpPath = getPathForSetUpTearDown(page, PageData.SUITE_SETUP_NAME);
    String tearDownPath = getPathForSetUpTearDown(page, PageData.SUITE_TEARDOWN_NAME);
    return setUpPath + "," + tearDownPath;
  }

  private String getPathForSetUpTearDown(WikiPage page, String setUpTearDownName) {
    String path = null;
    WikiPage suiteSetUpTearDown = page.getPageCrawler().getClosestInheritedPage(setUpTearDownName);
    if (suiteSetUpTearDown != null) {
      path = suiteSetUpTearDown.getFullPath().toString();
      setUpsAndTearDowns.putIfAbsent(path, suiteSetUpTearDown);
    }
    return path;
  }

  private List<WikiPage> reinsertPagesViaSetUpTearDownGroups(Map<String, List<WikiPage>> pageSetUpTearDownGroups) {
    List<WikiPage> pageList = new LinkedList<>();
    for (Map.Entry<String, List<WikiPage>> entry : pageSetUpTearDownGroups.entrySet()) {
      pageList.addAll(insertSetUpTearDownPageGroup(entry.getKey(), entry.getValue()));
    }
    return pageList;
  }

  private List<WikiPage> insertSetUpTearDownPageGroup(String setUpAndTearDownGroupKey, List<WikiPage> pageGroup) {
    List<WikiPage> pageList = new LinkedList<>();
    pageList.addAll(setUpForThisGroup(setUpAndTearDownGroupKey));
    pageList.addAll(pageGroup);
    pageList.addAll(tearDownForThisGroup(setUpAndTearDownGroupKey));
    return pageList;
  }

  private List<WikiPage> setUpForThisGroup(String setUpAndTearDown) {
    String setUpPath = setUpAndTearDown.split(",")[0];
    WikiPage setUpPage = setUpsAndTearDowns.get(setUpPath);
    return setUpPage != null ? Collections.singletonList(setUpPage) : Collections.emptyList();
  }

  private List<WikiPage>  tearDownForThisGroup(String setUpAndTearDownGroupKey) {
    String tearDownPath = setUpAndTearDownGroupKey.split(",")[1];
    WikiPage tearDownPage = setUpsAndTearDowns.get(tearDownPath);
    return tearDownPage != null ? Collections.singletonList(tearDownPage) : Collections.emptyList();
  }
}
