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
  private final List<WikiPage> pages;
  private final WikiPage root;
  private final Map<Descriptor, LinkedList<WikiPage>> pagesByTestSystem;
  private final DescriptorFactory descriptorFactory;

  public PagesByTestSystem(List<WikiPage> pages, WikiPage root, DescriptorFactory descriptorFactory) {
    this.pages = pages;
    this.root = root;
    this.descriptorFactory = descriptorFactory;
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
  }


  private Map<Descriptor, LinkedList<WikiPage>> mapWithAllPagesButSuiteSetUpAndTearDown() {
    Map<Descriptor, LinkedList<WikiPage>> pagesByTestSystem = new HashMap<Descriptor, LinkedList<WikiPage>>();

    for (WikiPage wikiPage : pages) {
      if (!SuiteContentsFinder.isSuiteSetupOrTearDown(wikiPage)) {
        Descriptor descriptor = descriptorFactory.create(wikiPage);
        getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(wikiPage);
      }
    }
    return pagesByTestSystem;
  }

  private void addPageToListWithinMap(Map<Descriptor, LinkedList<WikiPage>> pagesByTestSystem, WikiPage wikiPage) {
  }

  private LinkedList<WikiPage> getOrMakeListWithinMap(Map<Descriptor, LinkedList<WikiPage>> pagesByTestSystem, Descriptor descriptor) {
    LinkedList<WikiPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<WikiPage>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private Map<Descriptor, LinkedList<WikiPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<Descriptor, LinkedList<WikiPage>> pagesByTestSystem) {
    if (pages.size() > 0) {
      PageListSetUpTearDownSurrounder surrounder = new PageListSetUpTearDownSurrounder(root);

      for (LinkedList<WikiPage> pagesForTestSystem : pagesByTestSystem.values())
        surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pagesForTestSystem);
    }
    return pagesByTestSystem;
  }

  public int totalTestsToRun() {
    int tests = 0;
    for (LinkedList<WikiPage> listOfPagesToRun : pagesByTestSystem.values()) {
      tests += listOfPagesToRun.size();
    }
    return tests;
  }

  public Collection<Descriptor> descriptors() {
    return pagesByTestSystem.keySet();
  }

  public List<WikiPage> testPageForDescriptor(Descriptor descriptor) {
    return Collections.unmodifiableList(pagesByTestSystem.get(descriptor));
  }

  public static interface DescriptorFactory {
    Descriptor create(WikiPage page);
  }
}