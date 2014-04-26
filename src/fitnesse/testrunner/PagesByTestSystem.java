package fitnesse.testrunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.Descriptor;
import fitnesse.wiki.WikiPage;

/**
 * Organize pages by test system in an appropriate order.
 */
public class PagesByTestSystem {
  private final WikiPage root;
  private final Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem;

  public PagesByTestSystem(List<WikiPage> pages, WikiPage root) {
    this.root = root;
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown(pages));
  }

  private Map<WikiPageIdentity, List<WikiPage>> mapWithAllPagesButSuiteSetUpAndTearDown(List<WikiPage> pages) {
    Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem = new HashMap<WikiPageIdentity, List<WikiPage>>(2);

    for (WikiPage wikiPage : pages) {
      if (!SuiteContentsFinder.isSuiteSetupOrTearDown(wikiPage)) {
        WikiPageIdentity identity = new WikiPageIdentity(wikiPage.getData());
        getOrMakeListWithinMap(pagesByTestSystem, identity).add(wikiPage);
      }
    }
    return pagesByTestSystem;
  }

  private List<WikiPage> getOrMakeListWithinMap(Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem, WikiPageIdentity descriptor) {
    List<WikiPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<WikiPage>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private Map<WikiPageIdentity, List<WikiPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<WikiPageIdentity, List<WikiPage>> pagesByTestSystem) {
    Map<WikiPageIdentity, List<WikiPage>> orderedPagesByTestSystem = new HashMap<WikiPageIdentity, List<WikiPage>>(pagesByTestSystem.size());

    if (pagesByTestSystem.size() > 0) {
      PageListSetUpTearDownSurrounder surrounder = new PageListSetUpTearDownSurrounder(root);

      for (Map.Entry<WikiPageIdentity, List<WikiPage>> pages : pagesByTestSystem.entrySet())
        orderedPagesByTestSystem.put(pages.getKey(), surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pages.getValue()));
    }
    return orderedPagesByTestSystem;
  }

  public int totalTestsToRun() {
    int tests = 0;
    for (List<WikiPage> listOfPagesToRun : pagesByTestSystem.values()) {
      tests += listOfPagesToRun.size();
    }
    return tests;
  }

  public Collection<WikiPageIdentity> identities() {
    return pagesByTestSystem.keySet();
  }

  public List<WikiPage> testPagesForIdentity(WikiPageIdentity identity) {
    return Collections.unmodifiableList(pagesByTestSystem.get(identity));
  }

}