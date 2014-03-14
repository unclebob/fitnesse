// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SavePropertiesResponder implements SecureResponder {
  public Response makeResponse(FitNesseContext context, Request request) {
    SimpleResponse response = new SimpleResponse();
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.root.getPageCrawler().getPage(path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    PageData data = page.getData();
    saveAttributes(request, data);
    VersionInfo commitRecord = page.commit(data);
    response.addHeader("Current-Version", commitRecord.getName());
    context.recentChanges.updateRecentChanges(data);
    response.redirect(context.contextRoot, resource);

    return response;
  }

  private void saveAttributes(Request request, PageData data) {
    setPageTypeAttribute(request, data);

    List<String> attrs = new LinkedList<String>();
    attrs.addAll(Arrays.asList(PageData.NON_SECURITY_ATTRIBUTES));
    attrs.addAll(Arrays.asList(PageData.SECURITY_ATTRIBUTES));
    attrs.add(PageData.PropertyPRUNE);

    for (Iterator<String> i = attrs.iterator(); i.hasNext();) {
      String attribute = i.next();
      if (isChecked(request, attribute))
        data.setAttribute(attribute);
      else
        data.removeAttribute(attribute);
    }

    String suites = (String) request.getInput("Suites");
    data.setAttribute(PageData.PropertySUITES, suites);

    String helpText = (String) request.getInput("HelpText");
    data.setAttribute(PageData.PropertyHELP, helpText);
  }

  private void setPageTypeAttribute(Request request, PageData data) {
    String pageType = getPageType(request);

    if (pageType == null)
      return;

    List<String> types = new LinkedList<String>();
    types.addAll(Arrays.asList(PageData.PAGE_TYPE_ATTRIBUTES));
    data.setAttribute(pageType);

    for (Iterator<String> i = types.iterator(); i.hasNext();) {
      String type = i.next();
      if (!pageType.equals(type))
        data.removeAttribute(type);
    }
  }

  private String getPageType(Request request) {
    return (String) request.getInput(PageData.PAGE_TYPE_ATTRIBUTE);
  }

  private boolean isChecked(Request request, String name) {
    return (request.getInput(name) != null);
  }

  public SecureOperation getSecureOperation() {
    return new AlwaysSecureOperation();
  }
}
