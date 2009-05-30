package fitnesse.responders.search;

import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_OBSOLETE;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_SETUP;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_TEARDOWN;
import static fitnesse.responders.search.SearchFormResponder.IGNORED;

import java.io.StringWriter;
import java.util.ArrayList;
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
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class ExecuteSearchPropertiesResponder implements SecureResponder {
  private List<String> setUpPageNames;
  private List<String> tearDownPageNames;

  public ExecuteSearchPropertiesResponder() {
    setUpPageNames = Arrays.asList("SetUp", "SuiteSetUp");
    tearDownPageNames = Arrays.asList("TearDown", "SuiteTearDown");
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

    List<WikiPage> pages = new ArrayList<WikiPage>();
    queryPageTree(pages, page, pageTypes, attributes, suites, excludeSetUp,
        excludeTearDown);

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

  private void queryPageTree(List<WikiPage> matchingPages,
      WikiPage searchRootPage, List<String> requestedPageTypes,
      Map<String, Boolean> attributes, String[] suites, boolean excludeSetUp,
      boolean excludeTearDown) throws Exception {
    if (pageMatchesQuery(searchRootPage, requestedPageTypes, attributes,
        suites, excludeSetUp, excludeTearDown)) {
      matchingPages.add(searchRootPage);
    }

    List<WikiPage> children = searchRootPage.getChildren();
    for (WikiPage child : children) {
      queryPageTree(matchingPages, child, requestedPageTypes, attributes,
          suites, excludeSetUp, excludeTearDown);
    }
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
