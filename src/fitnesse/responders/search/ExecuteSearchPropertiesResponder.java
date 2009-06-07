package fitnesse.responders.search;

import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_OBSOLETE;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_SETUP;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_TEARDOWN;
import static fitnesse.responders.search.SearchFormResponder.IGNORED;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.components.AttributeWikiPageFinder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.WikiPage;

public class ExecuteSearchPropertiesResponder extends ChunkingResponder {

  public ExecuteSearchPropertiesResponder() {
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  private String makeHtml() throws Exception {

    List<String> pageTypes = getPageTypesFromInput(request);
    Map<String, Boolean> attributes = getAttributesFromInput(request);
    String[] suites = getSuitesFromInput(request);
    boolean excludeSetUp = getExcludeSetUpFromInput(request);
    boolean excludeTearDown = getExcludeTearDownFromInput(request);

    if (pageTypes == null && attributes.isEmpty() && suites == null) {
      HtmlPage resultsPage = context.htmlPageFactory.newPage();
      resultsPage.title.use("Search Page Properties: " + request);
      resultsPage.header.use(HtmlUtil.makeBreadCrumbsWithPageType(request
          .getResource(), "Search Page Properties Results"));
      resultsPage.main.add("No search properties were specified.");
      return resultsPage.html();
    }

    List<WikiPage> pages = new AttributeWikiPageFinder(pageTypes, attributes,
        suites, excludeSetUp, excludeTearDown).search(page);

    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = context.getVelocityEngine().getTemplate(
    "searchPropertiesResults.vm");

    velocityContext.put("pageTitle", new PageTitle(
    "Search Page Properties Results"));
    velocityContext.put("searchResults", pages);
    velocityContext.put("searchedRootPage", page);
    velocityContext.put("request", request);

    template.merge(velocityContext, writer);

    return writer.toString();
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
  protected void doSending() throws Exception {
    response.add(makeHtml());
    response.setMaxAge(0);
    response.closeAll();
  }

  @Override
  protected boolean shouldRespondWith404() {
    return false;
  }

}
