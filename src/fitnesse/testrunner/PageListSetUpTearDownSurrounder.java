package fitnesse.testrunner;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PageListSetUpTearDownSurrounder {

  public List<WikiPage> surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(List<WikiPage> pageList) {
    List<WikiPage> pagesToRun = new LinkedList<>();

    Map<WikiPage, WikiPage> tearDownAfter = findPagesBeforeTearDown(pageList);
    Set<WikiPage> setupsAdded = new HashSet<>();
    for (WikiPage page : pageList) {
      addSetUpIfNeeded(pagesToRun, setupsAdded, page);
      pagesToRun.add(page);
      addTearDownIfPossible(pagesToRun, tearDownAfter, page);
    }

    return pagesToRun;
  }

  private Map<WikiPage, WikiPage> findPagesBeforeTearDown(List<WikiPage> pages) {
    Map<WikiPage, WikiPage> pageBeforeSuiteTearDown = new HashMap<>();
    for (WikiPage page : pages) {
      getClosestInheritedPage(page, PageData.SUITE_TEARDOWN_NAME).ifPresent(suiteTearDown -> {
          pageBeforeSuiteTearDown.put(suiteTearDown, page);
        });
    }
    Map<WikiPage, WikiPage> tearDownAfter = new IdentityHashMap<>();
    for (Map.Entry<WikiPage, WikiPage> entry : pageBeforeSuiteTearDown.entrySet()) {
      tearDownAfter.put(entry.getValue(), entry.getKey());
    }
    return tearDownAfter;
  }

  private void addSetUpIfNeeded(List<WikiPage> pagesToRun, Set<WikiPage> setupsAdded, WikiPage page) {
    getClosestInheritedPage(page, PageData.SUITE_SETUP_NAME).ifPresent(suiteSetUp -> {
      if (setupsAdded.add(suiteSetUp)) {
        pagesToRun.add(suiteSetUp);
      }
    });
  }

  private void addTearDownIfPossible(List<WikiPage> pagesToRun, Map<WikiPage, WikiPage> tearDownAfter, WikiPage page) {
    WikiPage tearDown = tearDownAfter.get(page);
    if (tearDown != null) {
      pagesToRun.add(tearDown);
    }
  }

  private Optional<WikiPage> getClosestInheritedPage(WikiPage page, String pageName) {
    return Optional.ofNullable(page.getPageCrawler().getClosestInheritedPage(pageName));
  }
}
