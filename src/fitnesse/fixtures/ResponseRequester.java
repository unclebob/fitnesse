// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.ColumnFixture;
import fitnesse.FitNesseExpediter;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.testutil.MockSocket;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.Utils;

public class ResponseRequester extends ColumnFixture {
  public String uri;
  public String username;
  public String password;

  protected MockRequest request;

  public boolean valid() throws Exception {
    return status() == 200;
  }

  public void execute() throws Exception {
    setRequest(new MockRequest());

    details();

    if (username != null)
      request.setCredentials(username, password);

    request.parseRequestUri("/" + uri);
    WikiPagePath path = PathParser.parse(request.getResource()); // uri;
    FitnesseFixtureContext.page = FitnesseFixtureContext.root.getPageCrawler().getPage(FitnesseFixtureContext.root, path);
    FitNesseExpediter expediter = new FitNesseExpediter(new MockSocket(""), FitnesseFixtureContext.context);
    FitnesseFixtureContext.response = expediter.createGoodResponse(request);
    FitnesseFixtureContext.sender = new MockResponseSender();
    FitnesseFixtureContext.sender.doSending(FitnesseFixtureContext.response);
  }

  public int status() throws Exception {
    int status = FitnesseFixtureContext.response.getStatus();
    return status;
  }

  public String contents() throws Exception {
    return "<pre>" + Utils.escapeHTML(FitnesseFixtureContext.sender.sentData()) + "</pre>";
  }

  public String html() throws Exception {
    return FitnesseFixtureContext.sender.sentData();
  }

  protected void details() {
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setRequest(MockRequest request) {
    this.request = request;
  }
}
