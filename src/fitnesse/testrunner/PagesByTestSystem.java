package fitnesse.testrunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.wiki.ClassPathBuilder;
import fitnesse.wiki.WikiPage;

/**
 * Organize pages by test system in an appropriate order.
 */
public class PagesByTestSystem {
  private final List<WikiPage> pages;
  private final WikiPage root;
  private final boolean inProcess;
  private final boolean remoteDebug;
  private final Map<WikiPageDescriptor, LinkedList<WikiTestPage>> pagesByTestSystem;
  private final String classPath;

  public PagesByTestSystem(List<WikiPage> pages, WikiPage root, boolean inProcess, boolean remoteDebug) {
    this.pages = pages;
    this.root = root;
    this.inProcess = inProcess;
    this.remoteDebug = remoteDebug;
    this.classPath = new ClassPathBuilder().buildClassPath(pages);
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
  }


  private Map<WikiPageDescriptor, LinkedList<WikiTestPage>> mapWithAllPagesButSuiteSetUpAndTearDown() {
    Map<WikiPageDescriptor, LinkedList<WikiTestPage>> pagesByTestSystem = new HashMap<WikiPageDescriptor, LinkedList<WikiTestPage>>();

    for (WikiPage testPage : pages) {
      if (!SuiteContentsFinder.isSuiteSetupOrTearDown(testPage)) {
        addPageToListWithinMap(pagesByTestSystem, testPage);
      }
    }
    return pagesByTestSystem;
  }

  private void addPageToListWithinMap(Map<WikiPageDescriptor, LinkedList<WikiTestPage>> pagesByTestSystem, WikiPage wikiPage) {
    WikiTestPage testPage = new WikiTestPage(wikiPage);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(wikiPage.readOnlyData(), inProcess, remoteDebug, classPath);
    getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(testPage);
  }

  private LinkedList<WikiTestPage> getOrMakeListWithinMap(Map<WikiPageDescriptor, LinkedList<WikiTestPage>> pagesByTestSystem, WikiPageDescriptor descriptor) {
    LinkedList<WikiTestPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<WikiTestPage>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private Map<WikiPageDescriptor, LinkedList<WikiTestPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<WikiPageDescriptor, LinkedList<WikiTestPage>> pagesByTestSystem) {
    if (pages.size() > 0) {
      PageListSetUpTearDownSurrounder surrounder = new PageListSetUpTearDownSurrounder(root);

      for (LinkedList<WikiTestPage> pagesForTestSystem : pagesByTestSystem.values())
        surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pagesForTestSystem);
    }
    return pagesByTestSystem;
  }

  public int totalTestsToRun() {
    int tests = 0;
    for (LinkedList<WikiTestPage> listOfPagesToRun : pagesByTestSystem.values()) {
      tests += listOfPagesToRun.size();
    }
    return tests;
  }

  public Collection<WikiPageDescriptor> descriptors() {
    return pagesByTestSystem.keySet();
  }

  public List<WikiTestPage> testPageForDescriptor(WikiPageDescriptor descriptor) {
    return pagesByTestSystem.get(descriptor);
  }
}