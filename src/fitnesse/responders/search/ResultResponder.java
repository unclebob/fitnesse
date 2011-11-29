// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SearchObserver;
import fitnesse.html.HtmlPage;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.StringWriter;

public abstract class ResultResponder extends ChunkingResponder implements
  SearchObserver, SecureResponder {
  private int hits;

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  protected void doSending() throws Exception {
    HtmlPage htmlPage = context.htmlPageFactory.newPage();
    htmlPage.setTitle(getTitle());
    htmlPage.setPageTitle(new PageTitle(getTitle()) {
      public String getTitle() {
        return "search";
      }

      public String getLink() {
        return "search";
      }
    });
    htmlPage.setMainTemplate("searchResults.vm");

    if (page == null)
      page = context.root.getPageCrawler().getPage(context.root, PathParser.parse("FrontPage"));
    if (request.getQueryString() == null || request.getQueryString().equals(""))
      htmlPage.put("request", request.getBody());
    else
      htmlPage.put("request", request.getQueryString());
    htmlPage.put("page", page);
    
    htmlPage.divide();

    response.add(htmlPage.preDivision);

    startSearching();

    response.add(createSearchResultsFooter());
    response.add(htmlPage.postDivision);

    response.closeAll();
  }

  public void hit(WikiPage page) throws Exception {
    hits++;
    response.add(createSearchResultsEntry(page));
  }

  private String createSearchResultsFooter() throws Exception {
    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = VelocityFactory.getVelocityEngine().getTemplate(
      "searchResultsFooter.vm");
    velocityContext.put("hits", hits);

    template.merge(velocityContext, writer);

    return writer.toString();
  }
  
  private String createSearchResultsEntry(WikiPage result) throws Exception {
    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = VelocityFactory.getVelocityEngine().getTemplate("searchResultsEntry.vm");

    velocityContext.put("hits", hits);
    velocityContext.put("resultsRow", getRow());
    velocityContext.put("result", result);

    template.merge(velocityContext, writer);

    return writer.toString();
  }

  private int nextRow = 0;

  private int getRow() {
    return (nextRow++ % 2) + 1;
  }

  protected abstract String getTitle() throws Exception;

  protected abstract String getPageFooterInfo(int hits) throws Exception;

  protected void startSearching() throws Exception {
    hits = 0;
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
