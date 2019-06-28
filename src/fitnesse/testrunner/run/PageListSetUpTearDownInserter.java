package fitnesse.testrunner.run;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;

public class PageListSetUpTearDownInserter implements PageListSetUpTearDownProcessor {

  @Override
  public List<WikiPage> addSuiteSetUpsAndTearDowns(List<WikiPage> pageList) {
    List<WikiPage> pagesToRun = new LinkedList<>();

    Map<WikiPage, List<WikiPage>> tearDownAfter = findPagesBeforeTearDown(pageList);
    Set<WikiPage> setupsAdded = new HashSet<>();
    for (WikiPage page : pageList) {
      addSetUpIfNeeded(pagesToRun, setupsAdded, page);
      pagesToRun.add(page);
      addTearDownIfPossible(pagesToRun, tearDownAfter, page);
    }

    return pagesToRun;
  }

  private Map<WikiPage, List<WikiPage>> findPagesBeforeTearDown(List<WikiPage> pages) {
    Map<WikiPage, WikiPage> pageBeforeSuiteTearDown = new LinkedHashMap<>();
    for (WikiPage page : pages) {
      page.getPageCrawler()
        .traverseUncles(SUITE_TEARDOWN_NAME, suiteTearDown -> {
          pageBeforeSuiteTearDown.remove(suiteTearDown);
          pageBeforeSuiteTearDown.put(suiteTearDown, page);
        });
    }
    Map<WikiPage, List<WikiPage>> tearDownAfter = new IdentityHashMap<>();
    for (Map.Entry<WikiPage, WikiPage> entry : pageBeforeSuiteTearDown.entrySet()) {
      WikiPage normalPage = entry.getValue();
      WikiPage tearDownToRun = entry.getKey();
      tearDownAfter.computeIfAbsent(normalPage, p -> new ArrayList<>()).add(tearDownToRun);
    }
    return tearDownAfter;
  }

  private void addSetUpIfNeeded(List<WikiPage> pagesToRun, Set<WikiPage> setupsAdded, WikiPage page) {
    getClosestInheritedPage(page, PageData.SUITE_SETUP_NAME).ifPresent(suiteSetUp -> {
      if (setupsAdded.add(suiteSetUp)) {
        // add setups higher in tree if needed
        WikiPage parent = suiteSetUp.getParent();
        if (!parent.isRoot()) {
          addSetUpIfNeeded(pagesToRun, setupsAdded, parent.getParent());
        }

        pagesToRun.add(suiteSetUp);
      }
    });
  }

  private void addTearDownIfPossible(List<WikiPage> pagesToRun, Map<WikiPage, List<WikiPage>> tearDownAfter, WikiPage page) {
    List<WikiPage> tearDowns = tearDownAfter.get(page);
    if (tearDowns != null) {
      pagesToRun.addAll(tearDowns);
    }
  }

  private Optional<WikiPage> getClosestInheritedPage(WikiPage page, String pageName) {
    return Optional.ofNullable(page.getPageCrawler().getClosestInheritedPage(pageName));
  }
}
