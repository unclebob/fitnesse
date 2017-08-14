package fitnesse.testrunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;

/**
 * Organize pages by test system in an appropriate order.
 */
public class PagesByTestSystem {
  private final WikiPage root;
  private final Map<WikiPageIdentity, List<TestPage>> pagesByTestSystem;

  public PagesByTestSystem(List<WikiPage> pages, WikiPage root) {
    this.root = root;
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown(pages));
  }

  private Map<WikiPageIdentity, List<WikiPage>> mapWithAllPagesButSuiteSetUpAndTearDown(List<WikiPage> pages) {
    Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem = new HashMap<>(2);

    for (WikiPage wikiPage : pages) {
      if (!WikiTestPage.isSuiteSetupOrTearDown(wikiPage)) {
        WikiPageIdentity identity = new WikiPageIdentity(wikiPage);
        getOrMakeListWithinMap(pagesByTestSystem, identity).add(wikiPage);
      }
    }
    return pagesByTestSystem;
  }

  private List<WikiPage> getOrMakeListWithinMap(Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem, WikiPageIdentity descriptor) {
    List<WikiPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private Map<WikiPageIdentity, List<TestPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem) {
    Map<WikiPageIdentity, List<TestPage>> orderedPagesByTestSystem = new HashMap<>(pagesByTestSystem.size());

    if (!pagesByTestSystem.isEmpty()) {
      PageListSetUpTearDownSurrounder surrounder = new PageListSetUpTearDownSurrounder(root);

      for (Map.Entry<WikiPageIdentity, List<WikiPage>> pages : pagesByTestSystem.entrySet())
        orderedPagesByTestSystem.put(pages.getKey(), asTestPages(surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pages.getValue())));
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
