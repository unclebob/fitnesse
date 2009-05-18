package fitnesse.responders.search;

import static fitnesse.responders.search.SearchFormResponder.IGNORED;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_OBSOLETE;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_SETUP;
import static fitnesse.responders.search.SearchFormResponder.EXCLUDE_TEARDOWN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.StringUtil;
import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class ExecuteSearchPropertiesResponder implements SecureResponder {
  private String rootPagePath;
  private String resource;
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
    resource = request.getResource();
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
      return createResultsPageWithContent(context, request,
      "No search properties were specified.");
    }

    List<WikiPage> pages = new ArrayList<WikiPage>();
    queryPageTree(pages, page, pageTypes, attributes, suites, excludeSetUp,
        excludeTearDown);
    int matchingPages = pages.size();
    if (matchingPages == 0) {
      return createResultsPageWithContent(context, request,
      "No pages matched specified search properties.");
    }

    HtmlPage resultsPage = createResultsPage(context, request);
    HtmlTag form = makeForm();
    HtmlTag row = addTableToResults(form);
    addCellToTable(row, new HtmlTag("label",
        "Number of pages matching specified search properties: "
        + matchingPages));
    form.add(makeResultsTable(pages, page, attributes));
    resultsPage.main.add(form);

    resultsPage.main.add(makeQueryLinks(page, request));
    return resultsPage.html();
  }

  private String createResultsPageWithContent(FitNesseContext context,
      Request request, String text) throws Exception {
    HtmlPage resultsPage = createResultsPage(context, request);
    resultsPage.main.add(text);
    return resultsPage.html();
  }

  protected List<String> getPageTypesFromInput(Request request) {
    String requestedPageTypes = (String) request
    .getInput(SearchFormResponder.PAGE_TYPE);
    if (requestedPageTypes == null) {
      return null;
    }
    return Arrays.asList(requestedPageTypes.split(","));
  }

  private HtmlPage createResultsPage(FitNesseContext context, Request request)
  throws Exception {
    HtmlPage resultsPage = context.htmlPageFactory.newPage();
    resultsPage.title.use("Search Page Properties: " + request);
    resultsPage.header.use(HtmlUtil.makeBreadCrumbsWithPageType(request
        .getResource(), "Search Page Properties Results"));
    return resultsPage;
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

  private HtmlTag makeForm() {
    HtmlTag form = HtmlUtil.makeFormTag("post", resource);
    form.add(HtmlUtil.makeInputTag("hidden", "responder",
    "saveSearchProperties"));
    return form;
  }

  private HtmlTag addTableToResults(HtmlTag tag) {
    HtmlTag table = new HtmlTag("table");
    table.addAttribute("border", "0");
    table.addAttribute("cellspacing", "0");
    table.addAttribute("class", "dirListing");
    tag.add(table);

    HtmlTag row = new HtmlTag("tr");
    table.add(row);
    return row;
  }

  private void addCellToTable(HtmlTag row, HtmlTag tag) {
    HtmlTag cell = new HtmlTag("td");
    row.add(cell);
    cell.add(tag);
  }

  private HtmlTag makeResultsTable(List<WikiPage> pages, WikiPage page,
      Map<String, Boolean> attributes) throws Exception {
    rootPagePath = getFullPagePath(page.getParent());
    HtmlTableListingBuilder table = new HtmlTableListingBuilder();
    makeHeadingRow(table, attributes.keySet());
    makeMatchingPagesRows(table, pages, attributes);
    return table.getTable();
  }

  private HtmlTag makeQueryLinks(WikiPage page, Request request)
  throws Exception {
    TagGroup group = new TagGroup();
    group.add(HtmlUtil.HR);
    group.add(new HtmlTag("h4",
    "To save this search as a link, paste the text below into a page."));
    String pagePath = getFullPagePath(page);
    group.add(new HtmlTag("pre", String.format(
        "[[Search below !-%s-! for &lt;description&gt;][%s?%s]]", pagePath,
        pagePath, request.getBody())));

    String[] suiteQuery = getSuitesFromInput(request);
    if (suiteQuery != null && suiteQuery.length > 0) {
      group.add(HtmlUtil.HR);
      group.add(new HtmlTag("h4",
      "To test these pages, paste the text below into a page."));
      String queryString = (String) request
      .getInput(PropertiesResponder.SUITES);
      String testUrl = String.format("%s?suite&suiteFilter=%s", pagePath,
          queryString);
      group.add(new HtmlTag("pre", String.format(
          "[[Test !-%s-! under !-%s-!][%s]]", queryString, pagePath, testUrl)));
      group.add(HtmlUtil.BR);
      group.add("Or, just: ");
      group.add(HtmlUtil.makeLink(testUrl, "Test Now"));
    }

    return group;
  }

  private void makeHeadingRow(HtmlTableListingBuilder table,
      Set<String> attributesNames) throws Exception {
    List<HtmlTag> tags = new ArrayList<HtmlTag>();

    tags.add(new HtmlTag("strong", "Test"));
    tags.add(new HtmlTag("strong", "Page"));

    for (String attributeName : attributesNames) {
      tags.add(new HtmlTag("strong", attributeName));
    }

    tags.add(new HtmlTag("strong", "Tags"));

    addTagsToTableRow(table, tags);
  }

  private void makeMatchingPagesRows(HtmlTableListingBuilder table,
      List<WikiPage> pages, Map<String, Boolean> values) throws Exception {
    for (WikiPage page : pages) {
      PageData pageData = page.getData();
      makeRow(table, getFullPagePath(page), values,
          getSuitesProperty(pageData), pageData.hasAttribute("Test"), pageData
          .hasAttribute("Suite"));
    }
  }

  private String getSuitesProperty(PageData pageData) throws Exception {
    String suites = pageData.getAttribute(PropertiesResponder.SUITES);
    if (suites == null || suites.equals("null"))
      return "";

    return suites;
  }

  private void makeRow(HtmlTableListingBuilder table, String pageName,
      Map<String, Boolean> attributes, String suites, boolean isTest,
      boolean isSuite) throws Exception {
    List<HtmlTag> tags = new ArrayList<HtmlTag>();

    tags.add(generatePageTypeLinkTag(pageName, isTest, isSuite));

    tags.add(HtmlUtil.makeLink(pageName, new HtmlTag("label",
        getPageNameUnderRoot(pageName))));

    for (Map.Entry<String, Boolean> attribute : attributes.entrySet()) {
      tags.add(makeAttributeCheckbox(pageName + "_" + attribute.getKey(),
          attribute.getValue()));
    }

    tags.add(makeSuitesTextField(suites));

    addTagsToTableRow(table, tags);
  }

  private HtmlTag generatePageTypeLinkTag(String pageName, boolean isTest, boolean isSuite) {
    if (isSuite) {
      return HtmlUtil.makeLink(pageName + "?suite", new HtmlTag("label", "Suite"));
    }
    if (isTest) {
      return HtmlUtil.makeLink(pageName + "?test", new HtmlTag("label", "Test"));
    }
    return new HtmlTag("label", "");
  }

  private String getPageNameUnderRoot(String pageName) {
    return pageName.substring(rootPagePath.length());
  }

  private void addTagsToTableRow(HtmlTableListingBuilder table,
      List<HtmlTag> tags) throws Exception {
    table.addRow(tags.toArray(new HtmlTag[tags.size()]));
  }

  private HtmlTag makeSuitesTextField(String suites) {
    return new HtmlTag("label", suites);
  }

  private HtmlTag makeAttributeCheckbox(String name, boolean attributeOn) {
    HtmlTag checkbox = HtmlUtil.makeInputTag("checkbox", name);
    disableInputTag(checkbox);
    if (attributeOn)
      checkbox.addAttribute("checked", "true");
    return checkbox;
  }

  private void disableInputTag(HtmlTag checkbox) {
    checkbox.addAttribute("disabled", "true");
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

  private String getFullPagePath(WikiPage page) throws Exception {
    return StringUtil.join(page.getPageCrawler().getFullPath(page).getNames(),
    ".");
  }

}
