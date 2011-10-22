// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.RecentChanges;
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
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.root.getPageCrawler().getPage(context.root, path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    PageData data = page.getData();
    saveAttributes(request, data);
    VersionInfo commitRecord = page.commit(data);
    response.addHeader("Previous-Version", commitRecord.getName());
    RecentChanges.updateRecentChanges(data);
    response.redirect(resource);

    return response;
  }

  private void saveAttributes(Request request, PageData data) throws Exception {
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

    String value = (String) request.getInput(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
    value = value == null ? "" : value;
    if (!value.equals(data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE))) {
      WikiPage page = data.getWikiPage();
      if (page.hasExtension(VirtualCouplingExtension.NAME)) {
        VirtualCouplingExtension extension = (VirtualCouplingExtension) page.getExtension(VirtualCouplingExtension.NAME);
        extension.resetVirtualCoupling();
      }
    }
    if ("".equals(value) || value == null)
      data.removeAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
    else
      data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, value);

    String suites = (String) request.getInput("Suites");
    data.setAttribute(PageData.PropertySUITES, suites);

    String helpText = (String) request.getInput("HelpText");
    data.setAttribute(PageData.PropertyHELP, helpText);
  }

  private void setPageTypeAttribute(Request request, PageData data)
  throws Exception {
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
