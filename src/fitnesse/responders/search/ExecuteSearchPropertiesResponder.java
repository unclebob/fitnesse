package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.html.*;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.SecureResponder;
import fitnesse.responders.editing.PropertiesResponder;
import static fitnesse.responders.search.SearchFormResponder.ATTRIBUTE;
import static fitnesse.responders.search.SearchFormResponder.SELECTED;
import static fitnesse.responders.search.SearchFormResponder.*;
import fitnesse.util.StringUtil;
import fitnesse.wiki.*;

import java.util.*;

public class ExecuteSearchPropertiesResponder implements SecureResponder {
  private String rootPagePath;
  private String resource;
  private List<String> setUpTearDownPageNames;

  public ExecuteSearchPropertiesResponder() {
    setUpTearDownPageNames = Arrays.asList("SetUp", "TearDown", "SuiteSetUp", "SuiteTearDown");
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
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

  private WikiPage getWikiPageFromContext(FitNesseContext context, String resource) throws Exception {
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = context.root.getPageCrawler();
    WikiPage page;
    if (!crawler.pageExists(context.root, path)) {
      crawler.setDeadEndStrategy(new MockingPageCrawler());
      page = crawler.getPage(context.root, path);
    } else {
      page = crawler.getPage(context.root, path);
    }
    return page;
  }

  private String makeHtml(FitNesseContext context, Request request, WikiPage page) throws Exception {
    HtmlPage resultsPage = createResultsPage(context, request);

    Map<String, Boolean> attributes = getAttributesFromInput(request);
    String[] suites = getSuitesFromInput(request);
    boolean excludeSetUpTearDown = getExcludeSetUpTearDownFromInput(request);

    if (attributes.isEmpty() && suites == null) {
      addTextToResults(resultsPage, "No search properties were specified.");
    } else {
      List<WikiPage> pages = new ArrayList<WikiPage>();
      queryPageTree(pages, page, attributes, suites, excludeSetUpTearDown);
      int matchingPages = pages.size();

      if (matchingPages == 0) {
        addTextToResults(resultsPage, "No pages matched specified search properties.");
      } else {
        HtmlTag form = makeForm();
        HtmlTag row = addTableToResults(form);
        addCellToTable(row, new HtmlTag("label",
          "Number of pages matching specified search properties: " + matchingPages));
//                addCellToTable(row, HtmlUtil.makeSubmitButton("Save", "Save Modified Properties", "s"));
        form.add(makeResultsTable(pages, page, attributes));
        resultsPage.main.add(form);

        resultsPage.main.add(makeQueryLink(page, request));
      }
    }

    return resultsPage.html();
  }

  private HtmlPage createResultsPage(FitNesseContext context, Request request) throws Exception {
    HtmlPage resultsPage = context.htmlPageFactory.newPage();
    resultsPage.title.use("Search Page Properties: " + request);
    resultsPage.header.use(HtmlUtil.makeBreadCrumbsWithPageType(request.getResource(),
      "Search Page Properties Results"));
    return resultsPage;
  }

  private void queryPageTree(List<WikiPage> matchingPages, WikiPage searchRootPage,
                             Map<String, Boolean> attributes, String[] suites,
                             boolean excludeSetUpTearDown) throws Exception {
    if (pageMatchesQuery(searchRootPage, attributes, suites, excludeSetUpTearDown)) {
      matchingPages.add(searchRootPage);
    }

    List<WikiPage> children = searchRootPage.getChildren();
    for (WikiPage child : children) {
      queryPageTree(matchingPages, child, attributes, suites, excludeSetUpTearDown);
    }
  }

  protected boolean pageMatchesQuery(WikiPage page, Map<String, Boolean> inputs, String[] suites,
                                     boolean excludeSetUpTearDown) throws Exception {
    if (excludeSetUpTearDown && isSetUpOrTearDownPage(page)) {
      return false;
    }

    PageData pageData = page.getData();
    for (Map.Entry<String, Boolean> input : inputs.entrySet()) {
      if (!attributeMatchesInput(pageData.hasAttribute(input.getKey()), input.getValue()))
        return false;
    }

    return suitesMatchInput(pageData, suites);
  }

  private boolean isSetUpOrTearDownPage(WikiPage page) throws Exception {
    return setUpTearDownPageNames.contains(page.getName());
  }

  protected boolean attributeMatchesInput(boolean attributeSet, boolean inputValueOn) {
    return attributeSet == inputValueOn;
  }

  private boolean suitesMatchInput(PageData pageData, String[] suites)
    throws Exception {
    if (suites == null)
      return true;

    String suitesAttribute = pageData.getAttribute(PropertiesResponder.SUITES);
    List<String> suitesProperty = Arrays.asList(splitSuitesIntoArray(suitesAttribute));

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
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveSearchProperties"));
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

  private HtmlTag makeResultsTable(List<WikiPage> pages, WikiPage page, Map<String, Boolean> attributes)
    throws Exception {
    rootPagePath = getFullPagePath(page.getParent());
    HtmlTableListingBuilder table = new HtmlTableListingBuilder();
    makeHeadingRow(table, attributes.keySet());
    makeMatchingPagesRows(table, pages, attributes);
    return table.getTable();
  }

  private HtmlTag makeQueryLink(WikiPage page, Request request) throws Exception {
    TagGroup group = new TagGroup();
    group.add(HtmlUtil.HR);
    group.add(new HtmlTag("label", "To save this search as a link, paste the text below into a page."));
    group.add(HtmlUtil.BR);
    group.add(HtmlUtil.BR);
    group.add(new HtmlTag("label",
      "[[Search Properties]" +
        "[" + getFullPagePath(page) + "?" + request.getBody() + "]]"));
    return group;
  }

  private void addTextToResults(HtmlPage resultsPage, String text) {
    resultsPage.main.add(text);
  }

  private void makeHeadingRow(HtmlTableListingBuilder table, Set<String> attributesNames)
    throws Exception {
    List<HtmlTag> tags = new ArrayList<HtmlTag>();

    tags.add(new HtmlTag("strong", "Test"));
    tags.add(new HtmlTag("strong", "Page"));

    for (String attributeName : attributesNames) {
      tags.add(new HtmlTag("strong", attributeName));
    }

    tags.add(new HtmlTag("strong", PropertiesResponder.SUITES));

    addTagsToTableRow(table, tags);
  }

  private void makeMatchingPagesRows(HtmlTableListingBuilder table, List<WikiPage> pages, Map<String, Boolean> values)
    throws Exception {
    for (WikiPage page : pages) {
      PageData pageData = page.getData();
      makeRow(table, getFullPagePath(page), values, getSuitesProperty(pageData), pageData.hasAttribute("Suite"));
    }
  }

  private String getSuitesProperty(PageData pageData) throws Exception {
    String suites = pageData.getAttribute(PropertiesResponder.SUITES);
    if (suites == null || suites.equals("null"))
      return "";

    return suites;
  }

  private void makeRow(HtmlTableListingBuilder table, String pageName,
                       Map<String, Boolean> attributes, String suites,
                       boolean isSuite) throws Exception {
    List<HtmlTag> tags = new ArrayList<HtmlTag>();

    if (isSuite)
      tags.add(HtmlUtil.makeLink(pageName + "?suite", new HtmlTag("label", "Suite")));
    else
      tags.add(HtmlUtil.makeLink(pageName + "?test", new HtmlTag("label", "Test")));

    tags.add(HtmlUtil.makeLink(pageName, new HtmlTag("label", getPageNameUnderRoot(pageName))));

    for (Map.Entry<String, Boolean> attribute : attributes.entrySet()) {
      tags.add(makeAttributeCheckbox(pageName + "_" + attribute.getKey(),
        attribute.getValue()));
    }

    tags.add(makeSuitesTextField(suites));

    addTagsToTableRow(table, tags);
  }

  private String getPageNameUnderRoot(String pageName) {
    return pageName.substring(rootPagePath.length());
  }

  private void addTagsToTableRow(HtmlTableListingBuilder table, List<HtmlTag> tags) throws Exception {
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
    boolean suitesSelected = isChecked(request, PropertiesResponder.SUITES + SELECTED);
    if (!suitesSelected)
      return null;

    String suitesInput = (String) request.getInput(PropertiesResponder.SUITES);
    return splitSuitesIntoArray(suitesInput);
  }

  private String[] splitSuitesIntoArray(String suitesInput) {
    if (suitesInput == null || suitesInput.trim().length() == 0)
      return new String[0];

    return suitesInput.split("\\s*,\\s*");
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Boolean> getAttributesFromInput(Request request) {
    Map<String, Boolean> attributes = new LinkedHashMap<String, Boolean>();

    String[] attributeNames = StringUtil.combineArrays(WikiPage.PAGE_TYPE_ATTRIBUTES,
      WikiPage.NON_SECURITY_ATTRIBUTES,
      WikiPage.SECURITY_ATTRIBUTES);

    for (String attributeName : attributeNames) {
      if (request.hasInput(attributeName + ATTRIBUTE + SELECTED)) {
        attributes.put(attributeName, isChecked(request, attributeName + VALUE));
      }
    }

    return attributes;
  }

  private boolean getExcludeSetUpTearDownFromInput(Request request) {
    return request.hasInput(EXCLUDE_SET_UP_TEAR_DOWN);
  }

  private String getFullPagePath(WikiPage page) throws Exception {
    return StringUtil.join(page.getPageCrawler().getFullPath(page).getNames(), ".");
  }

  private boolean isChecked(Request request, String attributeName) {
    String attributeValue = (String) request.getInput(attributeName);
    return attributeValue != null && attributeValue.equals("on");
  }
}
