package fitnesse.responders.run;

import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.util.*;

public class PageListSetUpTearDownSurrounder {
  private WikiPage root;
  private List<WikiPage> pageList;

  public PageListSetUpTearDownSurrounder(WikiPage root) {
    this.root = root;
  }

  public void surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(List<WikiPage> pageList) throws Exception {
    this.pageList = pageList;
    Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups = new HashMap<String, LinkedList<WikiPage>>();
    createPageSetUpTearDownGroups(pageSetUpTearDownGroups);
    pageList.clear();
    reinsertPagesViaSetUpTearDownGroups(pageSetUpTearDownGroups);
  }

  private void createPageSetUpTearDownGroups(Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) throws Exception {
    for (WikiPage page : pageList) {
      makeSetUpTearDownPageGroupForPage(page, pageSetUpTearDownGroups);
    }
  }

  private void makeSetUpTearDownPageGroupForPage(WikiPage page, Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) throws Exception {
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

  private String getSetUpTearDownGroup(WikiPage page) throws Exception {
    String setUpPath = getPathForSetUpTearDown(page, SuiteContentsFinder.SUITE_SETUP_NAME);
    String tearDownPath = getPathForSetUpTearDown(page, SuiteContentsFinder.SUITE_TEARDOWN_NAME);
    return setUpPath + "," + tearDownPath;
  }

  private String getPathForSetUpTearDown(WikiPage page, String setUpTearDownName) throws Exception {
    String path = null;
    WikiPage suiteSetUpTearDown = PageCrawlerImpl.getClosestInheritedPage(setUpTearDownName, page);
    if (suiteSetUpTearDown != null)
      path = suiteSetUpTearDown.getPageCrawler().getFullPath(suiteSetUpTearDown).toString();
    return path;
  }

  private void reinsertPagesViaSetUpTearDownGroups(Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) throws Exception {
    Set groups = pageSetUpTearDownGroups.keySet();
    for (Object group : groups) {
      String setUpAndTearDownGroupKey = group.toString();
      LinkedList<WikiPage> pageGroup = pageSetUpTearDownGroups.get(group);
      insertSetUpTearDownPageGroup(setUpAndTearDownGroupKey, pageGroup);
    }
  }

  private void insertSetUpTearDownPageGroup(String setUpAndTearDownGroupKey, LinkedList<WikiPage> pageGroup) throws Exception {
    insertSetUpForThisGroup(setUpAndTearDownGroupKey);
    insertPagesOfThisGroup(pageGroup);
    insertTearDownForThisGroup(setUpAndTearDownGroupKey);
  }

  private void insertSetUpForThisGroup(String setUpAndTearDown) throws Exception {
    String setUpPath = setUpAndTearDown.split(",")[0];
    WikiPage setUpPage = root.getPageCrawler().getPage(root, PathParser.parse(setUpPath));
    if (setUpPage != null)
      pageList.add(setUpPage);
  }

  private void insertPagesOfThisGroup(LinkedList<WikiPage> pageGroup) {
    for (WikiPage page : pageGroup)
      pageList.add(page);
  }

  private void insertTearDownForThisGroup(String setUpAndTearDownGroupKey) throws Exception {
    String tearDownPath = setUpAndTearDownGroupKey.split(",")[1];
    WikiPage tearDownPage = root.getPageCrawler().getPage(root, PathParser.parse(tearDownPath));
    if (tearDownPage != null)
      pageList.add(tearDownPage);
  }
}