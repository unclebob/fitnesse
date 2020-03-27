// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.BasicResponder;
import fitnesse.wiki.*;

import static fitnesse.wiki.PageData.LAST_MODIFYING_USER;
import static fitnesse.wiki.PageData.PropertyLAST_MODIFIED;


public class VersionSelectionResponder extends BasicResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    WikiPage page = getPage(context, request);

    PageData pageData = page.getData();
    List<VersionInfo> versions = getVersionsList(page);

    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("Version Selection: " + request.getResource());
    html.setPageTitle(new PageTitle("Version Selection", PathParser.parse(request.getResource()), pageData.getAttribute(PageData.PropertySUITES)));
    html.put("lastModified", makeLastModifiedTag(pageData));
    html.put("versions", versions);
    html.setNavTemplate("viewNav");
    html.put("viewLocation", request.getResource());
    html.setMainTemplate("versionSelection");

    SimpleResponse response = new SimpleResponse();
    response.setContent(html.html(request));

    return response;
  }

  private String makeLastModifiedTag(PageData pageData) {
    String username = pageData.getAttribute(LAST_MODIFYING_USER);
    String dateString = pageData.getAttribute(PropertyLAST_MODIFIED);
  if (dateString == null) dateString ="";
  if (!dateString.isEmpty()){
  try {
    Date date = WikiPageProperty.getTimeFormat().parse(dateString);
    dateString = " on " + new SimpleDateFormat("MMM dd, yyyy").format(date) + " at " + new SimpleDateFormat("hh:mm:ss a").format(date);
  }
  catch (ParseException e) {
    dateString = " on " + dateString;
  }
  }
  if (username == null || "".equals(username))
  return "Last modified anonymously" + dateString;
  else
  return "Last modified by " + username + dateString;

  }

  public static List<VersionInfo> getVersionsList(WikiPage page) {
    List<VersionInfo> list = new ArrayList<>(page.getVersions());
    Collections.sort(list);
    Collections.reverse(list);
    return list;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
