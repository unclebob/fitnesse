// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlUtil;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;

public class MergeResponder implements Responder {
  private Request request;
  private String newContent;
  private String existingContent;
  private String resource;
  private Set<String> suites = new HashSet<>();;

  public MergeResponder(Request request) {
    this.request = request;
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    SimpleResponse response = new SimpleResponse();
    resource = this.request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.getRootPage().getPageCrawler().getPage(path);
    existingContent = page.getData().getContent();
    newContent = this.request.getInput(EditResponder.CONTENT_INPUT_NAME);
    // Combining the tags from the first edit with the second one
    String oldSuites = Optional
        .ofNullable(page.getData().getProperties().get(WikiPageProperty.SUITES))
        .orElse("");
    String newSuites = Optional
        .ofNullable(this.request.getInput(EditResponder.SUITES)).orElse("");
    Stream.of(oldSuites.split(",")).forEach(suites::add);
    Stream.of(newSuites.split(",")).forEach(suites::add);

    response.setContent(makePageHtml(context));

    return response;
  }

  private String makePageHtml(FitNesseContext context) {
    HtmlPage page = context.pageFactory.newPage();
    page.setTitle("Merge " + resource);
    page.setPageTitle(new PageTitle("Merge Changes", PathParser.parse(resource)));
    page.setMainTemplate("mergePage");
    page.put("editTime", SaveRecorder.timeStamp());
    page.put("ticketId", SaveRecorder.newTicket());
    page.put("oldContent", HtmlUtil.escapeHTML(existingContent));
    page.put("newContent", newContent);
    page.put(EditResponder.SUITES, suites);
    addHiddenAttributes(page);
    return page.html(request);
  }


  private void addHiddenAttributes(HtmlPage page) {
    if (request.hasInput(PageData.PAGE_TYPE_ATTRIBUTE)) {
      page.put("pageType", request.getInput(PageData.PAGE_TYPE_ATTRIBUTE));
    }

    List<String> attributes = new ArrayList<>();
    for (int i = 0; i < PageData.NON_SECURITY_ATTRIBUTES.length; i++) {
      String attribute = PageData.NON_SECURITY_ATTRIBUTES[i];
      if (request.hasInput(attribute))
        attributes.add(attribute);
    }
    if (request.hasInput(PageData.PropertyPRUNE))
      attributes.add(PageData.PropertyPRUNE);

    page.put("attributes", attributes);
  }
}
