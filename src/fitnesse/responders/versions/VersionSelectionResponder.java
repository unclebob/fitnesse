// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class VersionSelectionResponder implements Responder {
  private WikiPage page;
  private List<VersionInfo> versions;
  private List<String> ageStrings;
  private PageData pageData;
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    page = context.root.getPageCrawler().getPage(context.root, path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    pageData = page.getData();
    versions = getVersionsList(pageData);
    ageStrings = new ArrayList<String>();
    Date now = new GregorianCalendar().getTime();
    for (Iterator<VersionInfo> iterator = versions.iterator(); iterator.hasNext();) {
      VersionInfo version = iterator.next();
      ageStrings.add(howLongAgoString(now, version.getCreationTime()));
    }

    response.setContent(makeHtml(context));

    return response;
  }

  public String makeHtml(FitNesseContext context) throws Exception {
    HtmlPage html = context.htmlPageFactory.newPage();
    html.title.use("Version Selection: " + resource);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Version Selection"));
    html.main.use(makeRightColumn());
    return html.html();
  }

  public HtmlTag makeRightColumn() throws Exception {
    HtmlTag group = new TagGroup();
    group.add(new HtmlTag("h3", "Select a version."));

    HtmlTag form = new HtmlTag("form");
    String fullPathName = PathParser.render(page.getPageCrawler().getFullPath(page));
    form.addAttribute("action", fullPathName);
    form.addAttribute("method", "get");
    form.add(HtmlUtil.makeInputTag("hidden", "responder", "viewVersion"));

    HtmlTag table = new HtmlTag("table");
    table.addAttribute("cellspacing", "0");
    table.add(makeRow("th", new RawHtml("&nbsp;"), "Name", "Author", "Age"));

    for (int i = 0; i < versions.size(); i++)
      table.add(makeVersionRow(i));

    form.add(table);
    form.add(HtmlUtil.makeInputTag("submit", "save", "View Version"));

    group.add(form);

    return group;
  }

  private HtmlTag makeVersionRow(int index) {
    VersionInfo version = versions.get(index);
    HtmlTag input = HtmlUtil.makeInputTag("radio", "version", version.getName());
    return makeRow("td", input, version.getName(), version.getAuthor(), ageStrings.get(index).toString());
  }

  private HtmlTag makeRow(String cellType, HtmlElement input, String name, String author, String age) {
    HtmlTag row = new HtmlTag("tr");
    row.add(new HtmlTag(cellType, input));
    row.add(new HtmlTag(cellType, name));
    row.add(new HtmlTag(cellType, author));
    row.add(new HtmlTag(cellType, age));
    return row;
  }

  public static List<VersionInfo> getVersionsList(PageData data) {
    List<VersionInfo> list = new ArrayList<VersionInfo>(data.getVersions());
    Collections.sort(list);
    Collections.reverse(list);
    return list;
  }

  public static String howLongAgoString(Date now, Date then) {
    long time = Math.abs(now.getTime() - then.getTime()) / 1000;

    if (time < 60)
      return pluralize(time, "second");
    else if (time < 3600)
      return pluralize(time / 60, "minute");
    else if (time < 86400)
      return pluralize(time / (3600), "hour");
    else if (time < 31536000)
      return pluralize(time / (86400), "day");
    else
      return pluralize(time / (31536000), "year");
  }

  private static String pluralize(long time, String unit) {
    String age = time + " " + unit;
    if (time > 1)
      age = age + "s";

    return age;
  }

}
