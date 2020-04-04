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
import java.util.stream.Collectors;

/**
 * Organize pages by test system in an appropriate order.
 */
public class PagesByTestSystem {
  private final PageListSetUpTearDownProcessor processor;
  private final Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem;
  private final List<WikiPage> sourcePages;

  public PagesByTestSystem(List<WikiPage> sourcePages) {
    this(createProcessor(sourcePages), sourcePages);
  }

  public PagesByTestSystem(PageListSetUpTearDownProcessor processor, List<WikiPage> sourcePages) {
    this.sourcePages = sourcePages;
    this.processor = processor;
    Map<WikiPageIdentity, List<WikiPage>> testsPerSystem = mapWithAllPagesButSuiteSetUpAndTearDown(sourcePages);
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(testsPerSystem);
  }

  public static PageListSetUpTearDownProcessor createProcessor(List<WikiPage> pages) {
    return pages.isEmpty() || !TestPageWithSuiteSetUpAndTearDown.includeAllSetupsAndTearDowns(pages.get(0)) ?
      new PageListSetUpTearDownSurrounder() : new PageListSetUpTearDownInserter();
  }

  public static Map<WikiPageIdentity, List<WikiPage>> mapWithAllPagesButSuiteSetUpAndTearDown(List<WikiPage> pages) {
    Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem = new HashMap<>();

    for (WikiPage wikiPage : pages) {
      if (!wikiPage.isSuiteSetupOrTearDown()) {
        WikiPageIdentity identity = new WikiPageIdentity(wikiPage);
        pagesByTestSystem.computeIfAbsent(identity, i -> new LinkedList<>()).add(wikiPage);
      }
    }
    return pagesByTestSystem;
  }

  private Map<WikiPageIdentity, List<WikiPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<WikiPageIdentity, List<WikiPage>> testsPerSystem) {
    Map<WikiPageIdentity, List<WikiPage>> orderedPagesByTestSystem = new HashMap<>();

    if (!testsPerSystem.isEmpty()) {
      for (Map.Entry<WikiPageIdentity, List<WikiPage>> entry : testsPerSystem.entrySet()) {
        WikiPageIdentity system = entry.getKey();
        List<WikiPage> testPages = entry.getValue();
        List<WikiPage> allPages = processor.addSuiteSetUpsAndTearDowns(testPages);
        orderedPagesByTestSystem.put(system, allPages);
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

  public List<WikiPage> testsToRun() {
    return pagesByTestSystem.values().stream()
      .flatMap(List::stream)
      .collect(Collectors.toList());
  }

  public int totalTestsToRun() {
    return pagesByTestSystem.values().stream()
      .mapToInt(List::size).sum();
  }

  public Collection<WikiPageIdentity> identities() {
    return pagesByTestSystem.keySet();
  }

  public List<TestPage> testPagesForIdentity(WikiPageIdentity identity) {
    return asTestPages(pagesByTestSystem.get(identity));
  }

  public List<WikiPage> wikiPagesForIdentity(WikiPageIdentity identity) {
    return Collections.unmodifiableList(pagesByTestSystem.get(identity));
  }

  public List<WikiPage> getSourcePages() {
    return sourcePages;
  }
}
