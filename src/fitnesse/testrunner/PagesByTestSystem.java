package fitnesse.testrunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.Descriptor;
import fitnesse.wiki.ClassPathBuilder;
import fitnesse.wiki.WikiPage;

/**
 * Organize pages by test system in an appropriate order.
 */
public class PagesByTestSystem {
  private final List<WikiPage> pages;
  private final WikiPage root;
  private final Map<Descriptor, LinkedList<WikiTestPage>> pagesByTestSystem;
  private final DescriptorFactory descriptorFactory;

  public PagesByTestSystem(List<WikiPage> pages, WikiPage root, DescriptorFactory descriptorFactory) {
    this.pages = pages;
    this.root = root;
    this.descriptorFactory = descriptorFactory;
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
  }


  private Map<Descriptor, LinkedList<WikiTestPage>> mapWithAllPagesButSuiteSetUpAndTearDown() {
    Map<Descriptor, LinkedList<WikiTestPage>> pagesByTestSystem = new HashMap<Descriptor, LinkedList<WikiTestPage>>();

    for (WikiPage testPage : pages) {
      if (!SuiteContentsFinder.isSuiteSetupOrTearDown(testPage)) {
        addPageToListWithinMap(pagesByTestSystem, testPage);
      }
    }
    return pagesByTestSystem;
  }

  private void addPageToListWithinMap(Map<Descriptor, LinkedList<WikiTestPage>> pagesByTestSystem, WikiPage wikiPage) {
    WikiTestPage testPage = new WikiTestPage(wikiPage);
    Descriptor descriptor = descriptorFactory.create(wikiPage);

    getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(testPage);
  }

  private LinkedList<WikiTestPage> getOrMakeListWithinMap(Map<Descriptor, LinkedList<WikiTestPage>> pagesByTestSystem, Descriptor descriptor) {
    LinkedList<WikiTestPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<WikiTestPage>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private Map<Descriptor, LinkedList<WikiTestPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<Descriptor, LinkedList<WikiTestPage>> pagesByTestSystem) {
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

  public Collection<Descriptor> descriptors() {
    return pagesByTestSystem.keySet();
  }

  public List<WikiTestPage> testPageForDescriptor(Descriptor descriptor) {
    return pagesByTestSystem.get(descriptor);
  }

  public static interface DescriptorFactory {
    Descriptor create(WikiPage page);
  }
}