package fitnesse.responders.search;

import static fitnesse.responders.search.SearchFormResponder.SEARCH_ACTION_ATTRIBUTES;
import static fitnesse.responders.search.SearchFormResponder.SEARCH_ATTRIBUTE_SKIP;
import static fitnesse.responders.search.SearchFormResponder.SPECIAL_ATTRIBUTES;
import static fitnesse.wiki.PageData.PAGE_TYPE_ATTRIBUTE;
import static fitnesse.wiki.PageData.PropertySUITES;
import static fitnesse.wiki.PageData.SECURITY_ATTRIBUTES;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.wiki.search.AttributeWikiPageFinder;
import fitnesse.wiki.search.PageFinder;
import fitnesse.components.TraversalListener;
import fitnesse.http.Request;
import fitnesse.wiki.PageType;

public class ExecuteSearchPropertiesResponder extends ResultResponder {

  public static final String IGNORED = "Any";
  public static final String ACTION = "Action";
  public static final String SECURITY = "Security";
  public static final String SPECIAL = "Special";

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  protected List<PageType> getPageTypesFromInput(Request request) {
    String requestedPageTypes = (String) request.getInput(PAGE_TYPE_ATTRIBUTE);
    if (requestedPageTypes == null) {
      return null;
    }

    List<PageType> types = new ArrayList<PageType>();

    for (String type : requestedPageTypes.split(",")) {
      types.add(PageType.fromString(type));
    }
    return types;
  }

  protected String getSuitesFromInput(Request request) {
    if (!isSuitesGiven(request))
      return null;

    return (String) request.getInput(PropertySUITES);
  }

  private boolean isSuitesGiven(Request request) {
    return request.hasInput(PropertySUITES);
  }

  protected Map<String, Boolean> getAttributesFromInput(Request request) {
    Map<String, Boolean> attributes = new LinkedHashMap<String, Boolean>();

    getListboxAttributesFromRequest(request, ACTION, SEARCH_ACTION_ATTRIBUTES,
        attributes);
    getListboxAttributesFromRequest(request, SECURITY, SECURITY_ATTRIBUTES,
        attributes);

    getListboxAttributesFromRequest(request, SPECIAL, SPECIAL_ATTRIBUTES,
        attributes);

    // "obsolete" input is used to make existing queries work
    if (request.hasInput(SEARCH_ATTRIBUTE_SKIP) || request.hasInput("obsolete"))
      attributes.put(SEARCH_ATTRIBUTE_SKIP, true);

    return attributes;
  }

  private void getListboxAttributesFromRequest(Request request,
      String inputAttributeName, String[] attributeList,
      Map<String, Boolean> attributes) {
    String requested = (String) request.getInput(inputAttributeName);
    if (requested == null) {
      requested = IGNORED;
    }
    if (!IGNORED.equals(requested)) {
      for (String searchAttribute : attributeList) {
        attributes.put(searchAttribute, requested.contains(searchAttribute));
      }
    }
  }

  @Override
  protected String getTitle() {
    return "Search Page Properties Results";
  }

  @Override
  public void traverse(TraversalListener<Object> observer) {
    List<PageType> pageTypes = getPageTypesFromInput(request);
    Map<String, Boolean> attributes = getAttributesFromInput(request);
    String suites = getSuitesFromInput(request);

    if (pageTypes == null && attributes.isEmpty() && suites == null) {
      response.add("No search properties were specified.");
      return;
    }

    PageFinder finder = new AttributeWikiPageFinder(observer, pageTypes,
        attributes, suites);
    finder.search(page);
  }

}
