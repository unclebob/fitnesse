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
  private final Map<Descriptor, List<WikiPage>> pagesByTestSystem;
  private final DescriptorFactory descriptorFactory;

  public PagesByTestSystem(List<WikiPage> pages, WikiPage root, DescriptorFactory descriptorFactory) {
    this.pages = pages;
    this.root = root;
    this.descriptorFactory = descriptorFactory;
    this.pagesByTestSystem = addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
  }


  private Map<Descriptor, List<WikiPage>> mapWithAllPagesButSuiteSetUpAndTearDown() {
    Map<Descriptor, List<WikiPage>> pagesByTestSystem = new HashMap<Descriptor, List<WikiPage>>(2);

    for (WikiPage wikiPage : pages) {
      if (!SuiteContentsFinder.isSuiteSetupOrTearDown(wikiPage)) {
        Descriptor descriptor = descriptorFactory.create(wikiPage);
        getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(wikiPage);
      }
    }
    return pagesByTestSystem;
  }

  private List<WikiPage> getOrMakeListWithinMap(Map<Descriptor, List<WikiPage>> pagesByTestSystem, Descriptor descriptor) {
    List<WikiPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<WikiPage>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private Map<Descriptor, List<WikiPage>> addSuiteSetUpAndTearDownToAllTestSystems(Map<Descriptor, List<WikiPage>> pagesByTestSystem) {
    Map<Descriptor, List<WikiPage>> orderedPagesByTestSystem = new HashMap<Descriptor, List<WikiPage>>(pagesByTestSystem.size());

    if (pages.size() > 0) {
      PageListSetUpTearDownSurrounder surrounder = new PageListSetUpTearDownSurrounder(root);

      for (Map.Entry<Descriptor, List<WikiPage>> pages : pagesByTestSystem.entrySet())
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