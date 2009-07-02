package fitnesse.responders.search;

import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_OBSOLETE;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_SETUP;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_TEARDOWN;
import static fitnesse.responders.search.SearchFormResponder.IGNORED;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.components.AttributeWikiPageFinder;
import fitnesse.components.PageFinder;
import fitnesse.http.Request;
import fitnesse.responders.editing.PropertiesResponder;

public class ExecuteSearchPropertiesResponder extends ResultResponder {

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  protected List<String> getPageTypesFromInput(Request request) {
    String requestedPageTypes = (String) request
    .getInput(SearchFormResponder.PAGE_TYPE);
    if (requestedPageTypes == null) {
      return null;
    }
    return Arrays.asList(requestedPageTypes.split(","));
  }

  protected String[] getSuitesFromInput(Request request) {
    if (!suitesGiven(request))
      return null;

    String suitesInput = (String) request.getInput(PropertiesResponder.SUITES);
    return splitSuitesIntoArray(suitesInput);
  }

  private boolean suitesGiven(Request request) {
    return request.hasInput(PropertiesResponder.SUITES);
  }

  private String[] splitSuitesIntoArray(String suitesInput) {
    if (suitesInput == null || suitesInput.trim().length() == 0)
      return new String[0];

    return suitesInput.split("\\s*,\\s*");
  }

  protected Map<String, Boolean> getAttributesFromInput(Request request) {
    Map<String, Boolean> attributes = new LinkedHashMap<String, Boolean>();

    getListboxAttributesFromRequest(request, SearchFormResponder.ACTION,
        SearchFormResponder.ACTION_ATTRIBUTES, attributes);
    getListboxAttributesFromRequest(request, SearchFormResponder.SECURITY,
        SearchFormResponder.SECURITY_ATTRIBUTES, attributes);

    getObsoletePageAttributeFromInput(request, attributes);

    return attributes;
  }

  private void getListboxAttributesFromRequest(Request request,
      String inputAttributeName, String[] attributeList,
      Map<String, Boolean> attributes) {
    String requested = (String) request.getInput(inputAttributeName);
    if (requested == null) {
      requested = "";
    }
    if (!IGNORED.equals(requested)) {
      for (String searchAttribute : attributeList) {
        attributes.put(searchAttribute, requested.contains(searchAttribute));
      }
    }
  }

  private void getObsoletePageAttributeFromInput(Request request,
      Map<String, Boolean> attributes) {
    if (requestHasInputChecked(request, EXCLUDE_OBSOLETE)) {
      attributes.put("Pruned", false);
    }
  }

  private boolean getExcludeTearDownFromInput(Request request) {
    return requestHasInputChecked(request, EXCLUDE_TEARDOWN);
  }

  private boolean getExcludeSetUpFromInput(Request request) {
    return requestHasInputChecked(request, EXCLUDE_SETUP);
  }

  private boolean requestHasInputChecked(Request request, String checkBox) {
    return "on".equals(request.getInput(checkBox));
  }

  @Override
  protected String getPageFooterInfo(int hits) throws Exception {
    return "Found " + hits + " results for your search.";
  }

  @Override
  protected String getTitle() throws Exception {
    return "Search Page Properties Results";
  }

  @Override
  protected void startSearching() throws Exception {
    super.startSearching();
    List<String> pageTypes = getPageTypesFromInput(request);
    Map<String, Boolean> attributes = getAttributesFromInput(request);
    String[] suites = getSuitesFromInput(request);
    boolean excludeSetUp = getExcludeSetUpFromInput(request);
    boolean excludeTearDown = getExcludeTearDownFromInput(request);

    if (pageTypes == null && attributes.isEmpty() && suites == null) {
      response.add("No search properties were specified.");
      return;
    }

    PageFinder finder = new AttributeWikiPageFinder(this, pageTypes,
        attributes, suites, excludeSetUp, excludeTearDown);
    finder.search(page);
  }

}
