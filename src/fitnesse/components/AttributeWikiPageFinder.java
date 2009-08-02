package fitnesse.components;

import static fitnesse.wiki.PageData.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fitnesse.responders.search.ExecuteSearchPropertiesResponder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiPage;

public class AttributeWikiPageFinder extends WikiPageFinder {

  private static List<String> setUpPageNames = Arrays.asList("SetUp", "SuiteSetUp");
  private static List<String> tearDownPageNames = Arrays.asList("TearDown", "SuiteTearDown");

  private List<PageType> requestedPageTypes;
  private Map<String, Boolean> attributes;
  private List<String> suites;

  private static List<String> splitSuitesIntoArray(String suitesInput) {
    if (suitesInput == null)
      return null;

    if (isEmpty(suitesInput))
      return Collections.emptyList();

    return Arrays.asList(suitesInput.split("\\s*,\\s*"));
  }

  private static boolean isEmpty(String checkedString) {
    for (char character : checkedString.toCharArray()) {
      if (!Character.isWhitespace(character))
        return false;
    }
    return true;
  }

  public AttributeWikiPageFinder(SearchObserver observer,
      List<PageType> requestedPageTypes, Map<String, Boolean> attributes,
      List<String> suites) {
    super(observer);

    this.requestedPageTypes = requestedPageTypes;
    this.attributes = attributes;
    this.suites = suites;
  }

  public AttributeWikiPageFinder(ExecuteSearchPropertiesResponder observer,
      List<PageType> pageTypes, Map<String, Boolean> attributes, String suites) {
    this(observer, pageTypes, attributes, splitSuitesIntoArray(suites));
  }

  protected boolean pageMatches(WikiPage page) throws Exception {

    if (!meetsSetUpTearDownConditions(page)) {
      return false;
    }

    PageData pageData = page.getData();

    if (!pageIsOfRequestedPageType(page, requestedPageTypes)) {
      return false;
    }

    for (Map.Entry<String, Boolean> input : attributes.entrySet()) {
      if ("SetUp".equals(input.getKey()) || "TearDown".equals(input.getKey()))
        continue;

      if (!attributeMatchesInput(pageData.hasAttribute(input.getKey()), input
          .getValue()))
        return false;
    }

    return suitesMatchInput(pageData, suites);
  }

  private boolean meetsSetUpTearDownConditions(WikiPage page) throws Exception {

    if (attributes.containsKey("SetUp") && attributes.containsKey("TearDown")) {
      Boolean setupWanted = attributes.get("SetUp");
      Boolean teardownWanted = attributes.get("TearDown");

      if (setupWanted && teardownWanted) {
        return isSetUpPage(page) || isTearDownPage(page);
      }

      return isSetUpPage(page) == setupWanted && isTearDownPage(page) == teardownWanted;

    }

    if (attributes.containsKey("SetUp")) {
      return isSetUpPage(page) == attributes.get("SetUp");
    }

    if (attributes.containsKey("TearDown")) {
      return isTearDownPage(page) == attributes.get("TearDown");
    }

    return true;
  }

  private boolean isTearDownPage(WikiPage page) throws Exception {
    return tearDownPageNames.contains(page.getName());
  }

  private boolean isSetUpPage(WikiPage page) throws Exception {
    return setUpPageNames.contains(page.getName());
  }

  private boolean pageIsOfRequestedPageType(WikiPage page,
      List<PageType> requestedPageTypesEnum) throws Exception {
    PageType pageType = PageType.fromWikiPage(page);

    return (requestedPageTypesEnum.contains(pageType));
  }

  protected boolean attributeMatchesInput(boolean attributeSet,
      boolean inputValueOn) {
    return attributeSet == inputValueOn;
  }

  private boolean suitesMatchInput(PageData pageData, List<String> suites) throws Exception {
    if (suites == null)
      return true;

    List<String> suitesProperty = splitSuitesIntoArray(pageData.getAttribute(PropertySUITES));

    if (suites.isEmpty() != isEmptyOrNull(suitesProperty))
      return false;

    for (String suite : suites) {
      if (!suitesProperty.contains(suite))
        return false;
    }
    return true;
  }

  private boolean isEmptyOrNull(List<String> stringList) {
    return stringList == null || stringList.isEmpty();
  }

}
