package fitnesse.testrunner;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TestPageWithSuiteSetUpAndTearDown extends WikiTestPage {

  private List<WikiPage> suiteSetUps;
  private List<WikiPage> suiteTearDowns;

  public TestPageWithSuiteSetUpAndTearDown(WikiPage sourcePage) {
    super(sourcePage);
  }

  @Override
  protected void includeSetUps(StringBuilder decoratedContent) {
    List<WikiPage> setups = getSuiteSetUps();
    includePages("Suite Set Ups", setups, this::includeSetUp, decoratedContent);
    super.includeSetUps(decoratedContent);
  }

  @Override
  protected void includeTearDowns(StringBuilder decoratedContent) {
    super.includeTearDowns(decoratedContent);
    List<WikiPage> tearDowns = getSuiteTearDowns();
    includePages("Suite Tear Downs", tearDowns, this::includeTearDown, decoratedContent);
  }

  public List<WikiPage> getSuiteSetUps() {
    if (suiteSetUps == null && !isSuiteSetUpOrTearDownPage()) {
      if (includeAllSetupsAndTearDowns()) {
        suiteSetUps = findUncles(PageData.SUITE_SETUP_NAME);
      } else {
        suiteSetUps = getListOfNearestUncle(PageData.SUITE_SETUP_NAME);
      }
    }
    return suiteSetUps;
  }

  public List<WikiPage> getSuiteTearDowns() {
    if (suiteTearDowns == null && !isSuiteSetUpOrTearDownPage()) {
      if (includeAllSetupsAndTearDowns()) {
        List<WikiPage> uncles = findUncles(PageData.SUITE_TEARDOWN_NAME);
        Collections.reverse(uncles);
        suiteTearDowns = uncles;
      } else {
        this.suiteTearDowns = getListOfNearestUncle(PageData.SUITE_TEARDOWN_NAME);
      }
    }
    return suiteTearDowns;
  }

  private List<WikiPage> getListOfNearestUncle(String name) {
    return Optional.ofNullable(findInheritedPage(name))
      .map(Collections::singletonList)
      .orElseGet(Collections::emptyList);
  }

  private boolean includeAllSetupsAndTearDowns() {
    return includeAllSetupsAndTearDowns(getSourcePage());
  }

  public static boolean includeAllSetupsAndTearDowns(WikiPage page) {
    String allUncleSuiteSetups = page.getVariable("ALL_UNCLE_SUITE_SETUPS");
    return "true".equalsIgnoreCase(allUncleSuiteSetups);
  }

}
