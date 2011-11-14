package fitnesse.responders.run;

import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.util.*;

public class PageListSetUpTearDownSurrounder {
  private WikiPage root;
  private List<TestPage> pageList;

  public PageListSetUpTearDownSurrounder(WikiPage root) {
    this.root = root;
  }

  public void surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(List<TestPage> pageList) throws Exception {
    this.pageList = pageList;
    Map<String, LinkedList<TestPage>> pageSetUpTearDownGroups = new HashMap<String, LinkedList<TestPage>>();
    createPageSetUpTearDownGroups(pageSetUpTearDownGroups);
    pageList.clear();
    reinsertPagesViaSetUpTearDownGroups(pageSetUpTearDownGroups);
  }

  private void createPageSetUpTearDownGroups(Map<String, LinkedList<TestPage>> pageSetUpTearDownGroups) throws Exception {
    for (TestPage page : pageList) {
      makeSetUpTearDownPageGroupForPage(page, pageSetUpTearDownGroups);
    }
  }

  private void makeSetUpTearDownPageGroupForPage(TestPage page, Map<String, LinkedList<TestPage>> pageSetUpTearDownGroups) throws Exception {
    String group = getSetUpTearDownGroup(page.getSourcePage());
    LinkedList<TestPage> pageGroup;
    if (pageSetUpTearDownGroups.get(group) != null) {
      pageGroup = pageSetUpTearDownGroups.get(group);
      pageGroup.add(page);
    } else {
      pageGroup = new LinkedList<TestPage>();
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

  private void reinsertPagesViaSetUpTearDownGroups(Map<String, LinkedList<TestPage>> pageSetUpTearDownGroups) throws Exception {
    Set<String> groupKeys = pageSetUpTearDownGroups.keySet();
    for (String groupKey : groupKeys) {
      LinkedList<TestPage> pageGroup = pageSetUpTearDownGroups.get(groupKey);
      insertSetUpTearDownPageGroup(groupKey, pageGroup);
    }
  }

  private void insertSetUpTearDownPageGroup(String setUpAndTearDownGroupKey, LinkedList<TestPage> pageGroup) throws Exception {
    insertSetUpForThisGroup(setUpAndTearDownGroupKey);
    insertPagesOfThisGroup(pageGroup);
    insertTearDownForThisGroup(setUpAndTearDownGroupKey);
  }

  private void insertSetUpForThisGroup(String setUpAndTearDown) throws Exception {
    String setUpPath = setUpAndTearDown.split(",")[0];
    WikiPage setUpPage = root.getPageCrawler().getPage(root, PathParser.parse(setUpPath));
    if (setUpPage != null)
      pageList.add(new TestPage(setUpPage));
  }

  private void insertPagesOfThisGroup(LinkedList<TestPage> pageGroup) {
    for (TestPage page : pageGroup)
      pageList.add(page);
  }

  private void insertTearDownForThisGroup(String setUpAndTearDownGroupKey) throws Exception {
    String tearDownPath = setUpAndTearDownGroupKey.split(",")[1];
    WikiPage tearDownPage = root.getPageCrawler().getPage(root, PathParser.parse(tearDownPath));
    if (tearDownPage != null)
      pageList.add(new TestPage(tearDownPage));
  }
}