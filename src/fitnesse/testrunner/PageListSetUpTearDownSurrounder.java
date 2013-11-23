package fitnesse.testrunner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class PageListSetUpTearDownSurrounder {
  private WikiPage root;
  private List<WikiPage> pageList;

  public PageListSetUpTearDownSurrounder(WikiPage root) {
    this.root = root;
  }

  public void surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(List<WikiPage> pageList) {
    this.pageList = pageList;
    Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups = new HashMap<String, LinkedList<WikiPage>>();
    createPageSetUpTearDownGroups(pageSetUpTearDownGroups);
    pageList.clear();
    reinsertPagesViaSetUpTearDownGroups(pageSetUpTearDownGroups);
  }

  private void createPageSetUpTearDownGroups(Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) {
    for (WikiPage page : pageList) {
      makeSetUpTearDownPageGroupForPage(page, pageSetUpTearDownGroups);
    }
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

  private void reinsertPagesViaSetUpTearDownGroups(Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) {
    for (Map.Entry<String, LinkedList<WikiPage>> entry : pageSetUpTearDownGroups.entrySet()) {
      insertSetUpTearDownPageGroup(entry.getKey(), entry.getValue());
    }
  }

  private void insertSetUpTearDownPageGroup(String setUpAndTearDownGroupKey, LinkedList<WikiPage> pageGroup) {
    insertSetUpForThisGroup(setUpAndTearDownGroupKey);
    insertPagesOfThisGroup(pageGroup);
    insertTearDownForThisGroup(setUpAndTearDownGroupKey);
  }

  private void insertSetUpForThisGroup(String setUpAndTearDown) {
    String setUpPath = setUpAndTearDown.split(",")[0];
    WikiPage setUpPage = root.getPageCrawler().getPage(PathParser.parse(setUpPath));
    if (setUpPage != null)
      pageList.add(setUpPage);
  }

  private void insertPagesOfThisGroup(LinkedList<WikiPage> pageGroup) {
      pageList.addAll(pageGroup);
  }

  private void insertTearDownForThisGroup(String setUpAndTearDownGroupKey) {
    String tearDownPath = setUpAndTearDownGroupKey.split(",")[1];
    WikiPage tearDownPage = root.getPageCrawler().getPage(PathParser.parse(tearDownPath));
    if (tearDownPage != null)
      pageList.add(tearDownPage);
  }
}