// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

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
  public static final String ATTRIBUTE = "Attribute";
  public static final String SELECTED = "Selected";
  public static final String VALUE = "Value";
  public static final String EXCLUDE_SET_UP_TEAR_DOWN = "ExcludeSetUpTearDown";
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
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
    div.add("Tags:");
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
