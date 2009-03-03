package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.SecureResponder;
import fitnesse.wiki.*;

public class SearchPropertiesResponder implements SecureResponder {
  public static final String ATTRIBUTE = "Attribute";
  public static final String SELECTED = "Selected";
  public static final String VALUE = "Value";
  public static final String EXCLUDE_SET_UP_TEAR_DOWN = "ExcludeSetUpTearDown";

  private String resource;

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    resource = request.getResource();
    WikiPage page = getWikiPageFromContext(context, resource);

    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    String html = makeHtml(context, resource);

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
    } else
      page = crawler.getPage(context.root, path);
    return page;
  }

  private String makeHtml(FitNesseContext context, String resource) throws Exception {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.title.use("Search Page Properties: " + resource);
    page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Search Page Properties"));
    page.main.add(makeFormSections());

    return page.html();
  }

  private HtmlTag makeFormSections() throws Exception {
    TagGroup html = new TagGroup();
    html.add(makePropertiesForm());
    return html;
  }

  private HtmlTag makePropertiesForm() throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("post", resource);
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "executeSearchProperties"));

    HtmlTag trisection = new HtmlTag("div");
    trisection.addAttribute("style", "height: 300px");

    trisection.add(makeAttributeSelectionHtml("Page Type", WikiPage.PAGE_TYPE_ATTRIBUTES));
    trisection.add(makeAttributeSelectionHtml("Action", WikiPage.ACTION_ATTRIBUTES));
    trisection.add(makeAttributeSelectionHtml("Navigation", WikiPage.NAVIGATION_ATTRIBUTES));
    trisection.add(makeAttributeSelectionHtml("Security", WikiPage.SECURITY_ATTRIBUTES));
    trisection.add(makeSuitesSelectionHtml());
    form.add(trisection);

    form.add(HtmlUtil.BR);
    form.add(makeSetUpTearDownSelectionHtml());

    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("submit", "Search", "Search Properties"));
    return form;
  }

  public HtmlTag makeAttributeSelectionHtml(String propertyType, String[] attributes) throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left; width: 300px;");

    HtmlTag table = new HtmlTag("table");
    table.addAttribute("cellspacing", "1");
    table.addAttribute("border", "0");
    table.add(makeHeaderRow("Search?", propertyType, "Value"));
    makeAttributeSelectionHtml(table, attributes);
    div.add(table);

    return div;
  }

  private HtmlTag makeHeaderRow(String... titles) {
    HtmlTag row = new HtmlTag("tr");
    for (String title : titles) {
      row.add(new HtmlTag("th", title));
    }
    return row;
  }

  private void makeAttributeSelectionHtml(HtmlTag table, String[] attributes) throws Exception {
    for (String attributeName : attributes) {
      HtmlTag row = new HtmlTag("tr");
      row.add(makeRowCell(makeSelectionCheckbox(attributeName, ATTRIBUTE + SELECTED)));
      row.add(makeRowCell(" " + attributeName + " "));
      row.add(makeRowCell(makeSelectionCheckbox(attributeName, VALUE)));
      table.add(row);
    }
  }

  private HtmlTag makeRowCell(HtmlTag content) throws Exception {
    HtmlTag cell = new HtmlTag("td");
    cell.add(content);
    return cell;
  }

  private HtmlTag makeRowCell(String content) throws Exception {
    HtmlTag cell = new HtmlTag("td");
    cell.add(content);
    return cell;
  }

  private HtmlTag makeSelectionCheckbox(String attributeName, String selection) throws Exception {
    return HtmlUtil.makeInputTag("checkbox", attributeName + selection);
  }

  private HtmlTag makeSuitesSelectionHtml() {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left;");
    div.add(PropertiesResponder.SUITES + ":");
    div.add(HtmlUtil.BR);
    div.add(HtmlUtil.makeInputTag("checkbox", PropertiesResponder.SUITES + SELECTED));
    div.add(HtmlUtil.makeInputTag("text", PropertiesResponder.SUITES, ""));
    return div;
  }

  private HtmlTag makeSetUpTearDownSelectionHtml() {
    HtmlTag checkbox = HtmlUtil.makeInputTag("checkbox", EXCLUDE_SET_UP_TEAR_DOWN);
    checkbox.tail = "Exclude SetUp and TearDown pages from the search.";
    return checkbox;
  }
}
