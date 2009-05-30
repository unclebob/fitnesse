package fitnesse.responders.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class PageSearcher {

  private List<String> setUpPageNames;
  private List<String> tearDownPageNames;

  public PageSearcher() {
    setUpPageNames = Arrays.asList("SetUp", "SuiteSetUp");
    tearDownPageNames = Arrays.asList("TearDown", "SuiteTearDown");
  }

  public List<WikiPage> search(WikiPage searchRootPage,
      List<String> requestedPageTypes, Map<String, Boolean> attributes,
      String[] suites, boolean excludeSetUp, boolean excludeTearDown)
      throws Exception {
    List<WikiPage> matchingPages = new ArrayList<WikiPage>();
    if (pageMatchesQuery(searchRootPage, requestedPageTypes, attributes, suites, excludeSetUp, excludeTearDown)) {
      matchingPages.add(searchRootPage);
    }

    List<WikiPage> children = searchRootPage.getChildren();
    for (WikiPage child : children) {
      matchingPages.addAll(search(child, requestedPageTypes, attributes,
          suites, excludeSetUp, excludeTearDown));
    }
    return matchingPages;
  }

  protected boolean pageMatchesQuery(WikiPage page,
      List<String> requestedPageTypes, Map<String, Boolean> inputs,
      String[] suites, boolean excludeSetUp, boolean excludeTearDown)
  throws Exception {
    if (excludeSetUp && isSetUpPage(page)) {
      return false;
    }

    if (excludeTearDown && isTearDownPage(page)) {
      return false;
    }

    PageData pageData = page.getData();

    if (!pageIsOfRequestedPageType(page, requestedPageTypes)) {
      return false;
    }

    for (Map.Entry<String, Boolean> input : inputs.entrySet()) {
      if (!attributeMatchesInput(pageData.hasAttribute(input.getKey()), input
          .getValue()))
        return false;
    }

    return suitesMatchInput(pageData, suites);
  }

  private boolean isTearDownPage(WikiPage page) throws Exception {
    return tearDownPageNames.contains(page.getName());
  }

  private boolean isSetUpPage(WikiPage page) throws Exception {
    return setUpPageNames.contains(page.getName());
  }

  private boolean pageIsOfRequestedPageType(WikiPage page,
      List<String> requestedPageTypes) throws Exception {
    PageData data = page.getData();
    if (data.hasAttribute("Suite")) {
      return requestedPageTypes.contains("Suite");
    }

    if (data.hasAttribute("Test")) {
      return requestedPageTypes.contains("Test");
    }

    return requestedPageTypes.contains("Normal");
  }

  protected boolean attributeMatchesInput(boolean attributeSet,
      boolean inputValueOn) {
    return attributeSet == inputValueOn;
  }

  private boolean suitesMatchInput(PageData pageData, String[] suites)
  throws Exception {
    if (suites == null)
      return true;

    String suitesAttribute = pageData.getAttribute(PropertiesResponder.SUITES);
    List<String> suitesProperty = Arrays
    .asList(splitSuitesIntoArray(suitesAttribute));

    if (suites.length == 0 && suitesProperty.size() > 0)
      return false;

    for (String suite : suites) {
      if (!suitesProperty.contains(suite))
        return false;
    }
    return true;
  }

  private String[] splitSuitesIntoArray(String suitesInput) {
    if (suitesInput == null || suitesInput.trim().length() == 0)
      return new String[0];

    return suitesInput.split("\\s*,\\s*");
  }

}
