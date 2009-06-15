// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SearchObserver;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;

public abstract class ResultResponder extends ChunkingResponder implements
SearchObserver, SecureResponder {
  private int hits;

  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  protected void doSending() throws Exception {
    response.add(createSearchResultsHeader());

    startSearching();

    response.add(createSearchResultsFooter());
    response.closeAll();
  }

  private String createSearchResultsFooter() throws Exception {
    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = context.getVelocityEngine().getTemplate(
    "searchResultsFooter.vm");

    velocityContext.put("hits", hits);
    velocityContext.put("request", request);
    velocityContext.put("searchedRootPage", page);

    template.merge(velocityContext, writer);

    return writer.toString();
  }

  private String createSearchResultsHeader() throws Exception {
    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = context.getVelocityEngine().getTemplate(
    "searchResultsHeader.vm");

    velocityContext.put("page_title", getTitle());
    velocityContext.put("pageTitle", new PageTitle(getTitle()) {
      public String getTitle() {
        return "search";
      }

      public String getLink() {
        return "search";
      }
    });

    template.merge(velocityContext, writer);

    return writer.toString();
  }

  public static String getDateFormatJavascriptRegex() {
    return "/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/";
  }

  public void hit(WikiPage page) throws Exception {
    hits++;
    response.add(createSearchResultsEntry(page));
  }

  private String createSearchResultsEntry(WikiPage result) throws Exception {
    VelocityContext velocityContext = new VelocityContext();

    StringWriter writer = new StringWriter();

    Template template = context.getVelocityEngine().getTemplate(
    "searchResultsEntry.vm");

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
