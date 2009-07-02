package fitnesse.components;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class AttributeWikiPageFinder extends WikiPageFinder {

  private List<String> setUpPageNames;
  private List<String> tearDownPageNames;

  private List<String> requestedPageTypes;
  private Map<String, Boolean> attributes;
  private List<String> suites;
  private boolean excludeSetUp;
  private boolean excludeTearDown;

  public AttributeWikiPageFinder(SearchObserver observer, List<String> requestedPageTypes, Map<String, Boolean> attributes, String[] suites, boolean excludeSetUp, boolean excludeTearDown) {
    super(observer);
    setUpPageNames = Arrays.asList("SetUp", "SuiteSetUp");
    tearDownPageNames = Arrays.asList("TearDown", "SuiteTearDown");
    this.requestedPageTypes = requestedPageTypes;
    this.attributes = attributes;
    if (suites != null)
      this.suites = Arrays.asList(suites);
    this.excludeSetUp = excludeSetUp;
    this.excludeTearDown = excludeTearDown;
  }

  protected boolean pageMatches(WikiPage page) throws Exception {
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

    for (Map.Entry<String, Boolean> input : attributes.entrySet()) {
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

  private boolean suitesMatchInput(PageData pageData, List<String> suites)
  throws Exception {
    if (suites == null)
      return true;

    String suitesAttribute = pageData.getAttribute(PropertiesResponder.SUITES);
    List<String> suitesProperty = Arrays
    .asList(splitSuitesIntoArray(suitesAttribute));

    if (suites.isEmpty() && !suitesProperty.isEmpty())
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
