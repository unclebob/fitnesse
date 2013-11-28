package fitnesse.testrunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import static java.util.Arrays.asList;

public class PageListSetUpTearDownSurrounder {
  private final WikiPage root;

  public PageListSetUpTearDownSurrounder(WikiPage root) {
    this.root = root;
  }

  public List<WikiPage> surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(List<WikiPage> pageList) {
    Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups = createPageSetUpTearDownGroups(pageList);
    return reinsertPagesViaSetUpTearDownGroups(pageSetUpTearDownGroups);
  }

  private Map<String, LinkedList<WikiPage>> createPageSetUpTearDownGroups(List<WikiPage> pageList) {
    Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups = new HashMap<String, LinkedList<WikiPage>>();
    for (WikiPage page : pageList) {
      makeSetUpTearDownPageGroupForPage(page, pageSetUpTearDownGroups);
    }
    return pageSetUpTearDownGroups;
  }

  private void makeSetUpTearDownPageGroupForPage(WikiPage page, Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) {
    String group = getSetUpTearDownGroup(page);
    LinkedList<WikiPage> pageGroup;
    if (pageSetUpTearDownGroups.get(group) != null) {
      pageGroup = pageSetUpTearDownGroups.get(group);
      pageGroup.add(page);
    } else {
      pageGroup = new LinkedList<WikiPage>();
      pageGroup.add(page);
      pageSetUpTearDownGroups.put(group, pageGroup);
    }
  }

  private String getSetUpTearDownGroup(WikiPage page) {
    String setUpPath = getPathForSetUpTearDown(page, PageData.SUITE_SETUP_NAME);
    String tearDownPath = getPathForSetUpTearDown(page, PageData.SUITE_TEARDOWN_NAME);
    return setUpPath + "," + tearDownPath;
  }

  private String getPathForSetUpTearDown(WikiPage page, String setUpTearDownName) {
    String path = null;
    WikiPage suiteSetUpTearDown = page.getPageCrawler().getClosestInheritedPage(setUpTearDownName);
    if (suiteSetUpTearDown != null)
      path = suiteSetUpTearDown.getPageCrawler().getFullPath().toString();
    return path;
  }

  private List<WikiPage> reinsertPagesViaSetUpTearDownGroups(Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) {
    List<WikiPage> pageList = new LinkedList<WikiPage>();
    for (Map.Entry<String, LinkedList<WikiPage>> entry : pageSetUpTearDownGroups.entrySet()) {
      pageList.addAll(insertSetUpTearDownPageGroup(entry.getKey(), entry.getValue()));
    }
    return pageList;
  }

  private List<WikiPage> insertSetUpTearDownPageGroup(String setUpAndTearDownGroupKey, LinkedList<WikiPage> pageGroup) {
    List<WikiPage> pageList = new LinkedList<WikiPage>();
    pageList.addAll(setUpForThisGroup(setUpAndTearDownGroupKey));
    pageList.addAll(pageGroup);
    pageList.addAll(tearDownForThisGroup(setUpAndTearDownGroupKey));
    return pageList;
  }

  private List<WikiPage> setUpForThisGroup(String setUpAndTearDown) {

    String setUpPath = setUpAndTearDown.split(",")[0];
    WikiPage setUpPage = root.getPageCrawler().getPage(PathParser.parse(setUpPath));
    return setUpPage != null ? asList(setUpPage) : Collections.<WikiPage>emptyList();
  }

  private List<WikiPage>  tearDownForThisGroup(String setUpAndTearDownGroupKey) {
    String tearDownPath = setUpAndTearDownGroupKey.split(",")[1];
    WikiPage tearDownPage = root.getPageCrawler().getPage(PathParser.parse(tearDownPath));
    return tearDownPage != null ? asList(tearDownPage) : Collections.<WikiPage>emptyList();
  }
}