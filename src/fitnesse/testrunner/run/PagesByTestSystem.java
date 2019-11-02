package fitnesse.testrunner.run;

import fitnesse.testrunner.TestPageWithSuiteSetUpAndTearDown;
import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Organize pages by test system in an appropriate order.
 */
public class PagesByTestSystem {
  private final PageListSetUpTearDownProcessor processor;
  private final Map<WikiPageIdentity, List<TestPage>> pagesByTestSystem;

  public PagesByTestSystem(List<WikiPage> pages) {
    this(pages.isEmpty() || !TestPageWithSuiteSetUpAndTearDown.includeAllSetupsAndTearDowns(pages.get(0)) ?
        new PageListSetUpTearDownSurrounder() : new PageListSetUpTearDownInserter(),
      pages);
  }

  public PagesByTestSystem(PageListSetUpTearDownProcessor processor, List<WikiPage> pages) {
    this.processor = processor;
    Map<WikiPageIdentity, List<WikiPage>> testsPerSystem = mapWithAllPagesButSuiteSetUpAndTearDown(pages);
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(testsPerSystem);
  }

  private Map<WikiPageIdentity, List<WikiPage>> mapWithAllPagesButSuiteSetUpAndTearDown(List<WikiPage> pages) {
    Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem = new HashMap<>();

    for (WikiPage wikiPage : pages) {
      if (!WikiTestPage.isSuiteSetupOrTearDown(wikiPage)) {
        WikiPageIdentity identity = new WikiPageIdentity(wikiPage);
        pagesByTestSystem.computeIfAbsent(identity, i -> new LinkedList<>()).add(wikiPage);
      }
    }
    return pagesByTestSystem;
  }

  private Map<WikiPageIdentity, List<TestPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<WikiPageIdentity, List<WikiPage>> testsPerSystem) {
    Map<WikiPageIdentity, List<TestPage>> orderedPagesByTestSystem = new HashMap<>();

    if (!testsPerSystem.isEmpty()) {
      for (Map.Entry<WikiPageIdentity, List<WikiPage>> entry : testsPerSystem.entrySet()) {
        WikiPageIdentity system = entry.getKey();
        List<WikiPage> testPages = entry.getValue();
        List<WikiPage> allPages = processor.addSuiteSetUpsAndTearDowns(testPages);
        orderedPagesByTestSystem.put(system, asTestPages(allPages));
      }
    }
    return orderedPagesByTestSystem;
  }

  private List<TestPage> asTestPages(List<WikiPage> wikiPages) {
    List<TestPage> testPages = new ArrayList<>(wikiPages.size());
    for (WikiPage page : wikiPages) {
      // TODO: find the appropriate type of test page for this test system
      testPages.add(new WikiTestPage(page));
    }
    return testPages;
  }

  public int totalTestsToRun() {
    int tests = 0;
    for (List<TestPage> listOfPagesToRun : pagesByTestSystem.values()) {
      tests += listOfPagesToRun.size();
    }
    return tests;
  }

  public Collection<WikiPageIdentity> identities() {
    return pagesByTestSystem.keySet();
  }

  public List<TestPage> testPagesForIdentity(WikiPageIdentity identity) {
    return Collections.unmodifiableList(pagesByTestSystem.get(identity));
  }

}
