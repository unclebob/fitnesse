// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import java.util.Arrays;
import java.util.List;

import util.StringUtil;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.PropertiesResponder;
import fitnesse.wiki.WikiPage;

public class SearchFormResponder implements Responder {
  public static final String EXCLUDE_TEARDOWN = "ExcludeTearDown";
  public static final String EXCLUDE_SETUP = "ExcludeSetUp";
  public static final String EXCLUDE_OBSOLETE = "ExcludeObsolete";
  private static final String DON_T_CARE = "Don't care";
  public static String[] PAGE_TYPE_ATTRIBUTES = { "Normal", "Test", "Suite" };
  public static String[] ACTION_ATTRIBUTES = { "Edit", "Versions",
    "Properties", "Refactor", "WhereUsed", "RecentChanges", "Files", "Search" };
  public static String[] SECURITY_ATTRIBUTES = { WikiPage.SECURE_READ,
    WikiPage.SECURE_WRITE, WikiPage.SECURE_TEST };
  private String resource;
  public static String IGNORED = "Any";
  public static final String SECURITY = "Security";
  public static final String ACTION = "Action";
  public static final String PAGE_TYPE = "PageType";

  public Response makeResponse(FitNesseContext context, Request request)
  throws Exception {
    resource = request.getResource();
    SimpleResponse response = new SimpleResponse();
    response.setContent(makeSearchForm(context));

    return response;
  }

  private String makeSearchForm(FitNesseContext context) throws Exception {
    HtmlPage page = context.htmlPageFactory.newPage();
    page.body.addAttribute("onload", "document.forms[0].searchString.focus()");
    HtmlUtil.addTitles(page, "Search Form");
    page.main.use(makeRightColumn());
    page.main.add(new HtmlTag("hr"));
    HtmlTag searchPropertiesHeader = new HtmlTag("h2");
    searchPropertiesHeader.add("Search Properties");
    page.main.add(searchPropertiesHeader);
    page.main.add(makeSearchPropertiesForm());
    return page.html();
  }

  private HtmlTag makeRightColumn() {
    HtmlTag form = new HtmlTag("form");
    form.addAttribute("action", "search");
    form.addAttribute("method", "post");
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "search"));

    form.add("Search String:");
    form.add(HtmlUtil.makeInputTag("text", "searchString", ""));
    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.makeInputTag("submit", "searchType", "Search Titles!"));
    form.add(HtmlUtil.makeInputTag("submit", "searchType", "Search Content!"));

    form.add(HtmlUtil.BR);
    form.add(HtmlUtil.BR);
    form.add(new HtmlTag("b", "Search Titles!: "));
    form.add("Searches in page titles only.  Will run fairly quickly.");
    form.add(HtmlUtil.BR);
    form.add(new HtmlTag("b", "Search Content!: "));
    form.add("Searches in the content of every page.  Don't hold your breath.");

    return form;
  }

  private HtmlTag makeSearchPropertiesForm() throws Exception {
    TagGroup html = new TagGroup();
    html.add(makePropertiesForm());
    return html;
  }

  private HtmlTag makePropertiesForm() throws Exception {
    HtmlTag form = HtmlUtil.makeFormTag("post", resource);
    form.add(HtmlUtil.makeInputTag("hidden", "responder",
    "executeSearchProperties"));

    HtmlTag twosection = new HtmlTag("div");
    twosection.addAttribute("style", "height: 150px;");

    twosection.add(makeSuitesSelectionHtml());
    twosection.add(makeExclusionsAndSubmitSection());
    form.add(twosection);

    HtmlTag trisection = new HtmlTag("div");

    trisection.add(makeAttributeSelectionHtml(PAGE_TYPE, PAGE_TYPE_ATTRIBUTES,
        PAGE_TYPE_ATTRIBUTES));
    trisection
    .add(makeDontCareAttributeSelectionHtml(ACTION, ACTION_ATTRIBUTES));
    trisection.add(makeDontCareAttributeSelectionHtml(SECURITY,
        SECURITY_ATTRIBUTES));
    form.add(trisection);

    return form;
  }

  private HtmlTag makeExclusionsAndSubmitSection() throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left; width: 180px;");

    HtmlTag exclusionsTable = generateTable("Exclusions");
    HtmlTag row = new HtmlTag("tr");
    HtmlTag cell = new HtmlTag("td");
    cell.add(makeCheckboxWithDescription(EXCLUDE_OBSOLETE, "Obsolete tests"));
    cell.add(HtmlUtil.BR);
    cell.add(makeCheckboxWithDescription(EXCLUDE_SETUP, "SetUp pages"));
    cell.add(HtmlUtil.BR);
    cell.add(makeCheckboxWithDescription(EXCLUDE_TEARDOWN, "TearDown pages"));
    cell.add(HtmlUtil.BR);
    row.add(cell);
    exclusionsTable.add(row);

    div.add(exclusionsTable);

    div.add(HtmlUtil.BR);
    div.add(HtmlUtil.makeInputTag("submit", "Search", "Search Properties"));

    return div;
  }

  private HtmlTag makeCheckboxWithDescription(String attributeName,
      String description) {
    HtmlTag item;
    item = HtmlUtil.makeInputTag("checkbox", attributeName);
    item.add(description);
    return item;
  }

  private HtmlTag generateTable(String headerText) {
    HtmlTag pageTypeTable = new HtmlTag("table");
    pageTypeTable.addAttribute("cellspacing", "1");
    pageTypeTable.addAttribute("border", "0");
    pageTypeTable.add(makeHeaderRow(headerText));
    return pageTypeTable;
  }

  private HtmlTag makeDontCareAttributeSelectionHtml(String propertyType,
      String[] attributes) throws Exception {
    String[] dontCare = { DON_T_CARE };
    return makeAttributeSelectionHtml(propertyType, StringUtil.combineArrays(
        dontCare, attributes), dontCare);
  }

  public HtmlTag makeAttributeSelectionHtml(String propertyType,
      String[] attributes, String[] selection) throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left; width: 150px;");

    HtmlTag table = generateTable(propertyType);
    makeAttributeSelectionHtml(table, propertyType, attributes, Arrays
        .asList(selection));
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

  private void makeAttributeSelectionHtml(HtmlTag table, String propertyType,
      String[] attributes, List<String> selected) throws Exception {
    HtmlTag row = new HtmlTag("tr");
    HtmlTag selection = new HtmlTag("select");
    selection.addAttribute("name", propertyType);
    selection.addAttribute("id", propertyType);
    selection.addAttribute("multiple", "multiple");
    selection.addAttribute("size", Integer.toString(attributes.length));

    for (String attributeName : attributes) {
      HtmlTag option = new HtmlTag("option");
      if (DON_T_CARE.equals(attributeName)) {
        option.addAttribute("value", SearchFormResponder.IGNORED);
      }
      if (selected.contains(attributeName)) {
        option.addAttribute("selected", "selected");
      }
      option.add(attributeName);
      selection.add(option);
    }
    row.add(makeRowCell(selection));
    table.add(row);
  }

  private HtmlTag makeRowCell(HtmlTag content) throws Exception {
    HtmlTag cell = new HtmlTag("td");
    cell.add(content);
    return cell;
  }

  private HtmlTag makeSuitesSelectionHtml() throws Exception {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("style", "float: left; margin-right: 20px");
    HtmlTag suitesTable = generateTable("Tags");

    HtmlTag textArea = new HtmlTag("textarea");
    textArea.addAttribute("name", PropertiesResponder.SUITES);
    textArea.addAttribute("cols", "20");
    textArea.addAttribute("rows", "3");
    textArea.add(""); // this makes the textarea render correctly w/o filling in
    // the remaining text into it
    HtmlTag column = new HtmlTag("tr");
    HtmlTag cell = makeRowCell(textArea);
    cell.addAttribute("style", "padding-right: 15px;");
    column.add(cell);
    suitesTable.add(column);

    div.add(suitesTable);
    return div;
  }

}
