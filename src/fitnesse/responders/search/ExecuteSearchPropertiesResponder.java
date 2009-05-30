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

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.AttributeWikiPageFinder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class ExecuteSearchPropertiesResponder implements SecureResponder {

  public ExecuteSearchPropertiesResponder() {
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public Response makeResponse(FitNesseContext context, Request request)
  throws Exception {
    SimpleResponse response = new SimpleResponse();
    WikiPage page = getWikiPageFromContext(context, request.getResource());

    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    String html = makeHtml(context, request, page);

    response.setContent(html);
    response.setMaxAge(0);

    return response;
  }

  private WikiPage getWikiPageFromContext(FitNesseContext context,
      String resource) throws Exception {
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = context.root.getPageCrawler();
    if (!crawler.pageExists(context.root, path)) {
      crawler.setDeadEndStrategy(new MockingPageCrawler());
    }

    WikiPage page = crawler.getPage(context.root, path);
    return page;
  }

  private String makeHtml(FitNesseContext context, Request request,
      WikiPage page) throws Exception {

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

    List<WikiPage> pages = new AttributeWikiPageFinder(pageTypes, attributes, suites, excludeSetUp, excludeTearDown).search(page);

    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = context.getVelocityEngine().getTemplate(
    "searchResults.vm");

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

}
