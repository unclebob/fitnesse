// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.text.SimpleDateFormat;
import java.util.Date;

import fitnesse.FitNesseExpediter;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.responders.editing.EditResponder;
import fitnesse.util.MockSocket;
import fitnesse.util.SerialExecutorService;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.json.JSONObject;

public class PageDriver {
  private PageCreator creator = new PageCreator();
  private ResponseRequester requester = new ResponseRequester();
  private ResponseExaminer examiner = new ResponseExaminer();

  public void createPageWithContent(String pageName, String content) throws Exception {
    creator.pageName = pageName;
    creator.pageContents = content;
    creator.valid();
  }

  public void createPageWithAuthentication(String pageName, String attributes) throws Exception {
    creator.pageAttributes = attributes;
    creator.pageContents = "nothing";
    createPageWithContent(pageName, "");
  }

  public int requestPage(String uri) throws Exception {
    requester.uri = uri;
    requester.execute();
    return requester.status();
  }

  public int requestPageAuthenticatedByUserAndPassword(String uri, String user, String password) throws Exception {
    requester.username = user;
    requester.password = password;
    return requestPage(uri);
  }

  public int requestPageSaveWithContentsByUserAndPassword(String pageName, String contents, String username, String password) throws Exception {
    MockRequest request = new MockRequest();
    if (username != null)
      request.setCredentials(username, password);
    request.addInput("responder", "saveData");
    request.addInput(EditResponder.TIME_STAMP, "9999999999999");
    request.addInput(EditResponder.TICKET_ID, "321");
    request.addInput("pageContent", contents);
    request.parseRequestUri("/" + pageName);
    WikiPagePath path = PathParser.parse(request.getResource()); // uri;
    FitnesseFixtureContext.page = FitnesseFixtureContext.context.getRootPage().getPageCrawler().getPage(path);
    FitNesseExpediter expediter = new FitNesseExpediter(new MockSocket(""), FitnesseFixtureContext.context, new SerialExecutorService());
    FitnesseFixtureContext.response = expediter.createGoodResponse(request);
    FitnesseFixtureContext.sender = new MockResponseSender();
    FitnesseFixtureContext.sender.doSending(FitnesseFixtureContext.response);
    return FitnesseFixtureContext.response.getStatus();
  }

  public int requestPageSaveWithContents(String pageName, String contents) throws Exception {
    return requestPageSaveWithContentsByUserAndPassword(pageName, contents, null, null);
  }

  public String lastModifiedOfPage(String pageName) throws Exception {
    WikiPage root = FitnesseFixtureContext.context.getRootPage();
    WikiPagePath pagePath = PathParser.parse(pageName);
    WikiPage thePage = root.getPageCrawler().getPage(pagePath);
    PageData data = thePage.getData();
    return data.getAttribute(PageData.LAST_MODIFYING_USER);
  }

  public boolean pageIsASymbolicLink(String pageName) {
    WikiPage root = FitnesseFixtureContext.context.getRootPage();
    WikiPagePath pagePath = PathParser.parse(pageName);
    WikiPage thePage = root.getPageCrawler().getPage(pagePath);
    return thePage instanceof SymbolicPage;
  }

  public boolean pageExists(String pageName) {
	    WikiPage root = FitnesseFixtureContext.context.getRootPage();
	    WikiPagePath pagePath = PathParser.parse(pageName);
	    WikiPage thePage = root.getPageCrawler().getPage(pagePath);
	    return thePage != null;
  }

  public void makeATestPage(String pageName) throws Exception {
	onPageSetAttribute(pageName, "Test");
  }

  public void makeASuitePage(String pageName) throws Exception {
	onPageSetAttribute(pageName, "Suite");
  }

  private void onPageSetAttribute(String pageName, String attrName) {
    WikiPage root = FitnesseFixtureContext.context.getRootPage();
    WikiPagePath pagePath = PathParser.parse(pageName);
    WikiPage thePage = root.getPageCrawler().getPage(pagePath);
    PageData data = thePage.getData();
    data.setAttribute(attrName, "true");
    thePage.commit(data);
  }

  public boolean contentMatches(String pattern) throws Exception {
    examiner.type = "contents";
    examiner.pattern = pattern;
    return examiner.matches();
  }

  public String extractMatch(String pattern, String type, int group) throws Exception {
    examiner.type = type;
    examiner.pattern = pattern;
    return examiner.found(group);
  }

  public boolean contentContains(String subString) throws Exception {
    examiner.type = "contents";
    examiner.extractValueFromResponse();
    return examiner.getValue().contains(subString);
  }

  public boolean htmlContains(String subString) throws Exception {
    String html = requester.html();
    html = html.replaceAll("\n", " ");
    html = html.replaceAll("\r", " ");
    html = html.replaceAll("\\s+", " ");
    System.out.println("html = " + html);
    System.out.println("subString = " + subString);
    return (html.contains(subString));
  }

  public boolean htmlWithoutContains(String strip, String subString)
      throws Exception {
    String html = requester.html();
    html = html.replaceAll(strip, "");
    subString = subString.replaceAll(strip, "");
    return (html.contains(subString));
  }

  public boolean containsJsonPacket(String packet) throws Exception {
    packet = ResponseExaminer.convertBreaksToLineSeparators(packet);
    JSONObject expected = new JSONObject(packet);
    String contentString = requester.contents();
    int jsonStart = contentString.indexOf("{");
    if (jsonStart == -1)
      return false;
    contentString = contentString.substring(jsonStart);
    JSONObject actual = new JSONObject(contentString);
    return expected.toString(1).equals(actual.toString(1));
  }

  public String content() throws Exception {
    return requester.html();
  }

  public String lineIs(int lineNumber) throws Exception {
    examiner.type = "line";
    examiner.number = lineNumber;
    return examiner.string();
  }

  public int lineNumberContaining(String text) throws Exception {
    String content = requester.html();
    int textPosition = content.indexOf(text);
    if (textPosition == -1)
      return -1;
    String priorToContent = content.substring(0, textPosition);
    String[] lines = priorToContent.split("\n");
    return lines.length;
  }

  public int countOfTagWithIdPrefix(String tag, String idPrefix) throws Exception {
    NodeFilter filter =
      new AndFilter(
        new TagNameFilter(tag),
        new HasAttributePrefixFilter("id", idPrefix));
    return getMatchingTags(filter).size();
  }

  private NodeList getMatchingTags(NodeFilter filter) throws Exception {
    String html = examiner.html();
    Parser parser = new Parser(new Lexer(new Page(html)));
    NodeList list = parser.parse(null);
    NodeList matches = list.extractAllNodesThatMatch(filter, true);
    return matches;
  }

  public String pageHistoryDateSignatureOf(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(fitnesse.reporting.history.PageHistory.TEST_RESULT_FILE_DATE_PATTERN);
    return dateFormat.format(date);
  }

  public int countOfTagWithClassBelowTagWithIdPrefix(String childTag, String tagClass, String parentTag, String parentIdPrefix) throws Exception {
    NodeList parents = getMatchingTags(
            new AndFilter(
                    new TagNameFilter(parentTag),
                    new HasAttributePrefixFilter("id", parentIdPrefix))
    );

    NodeFilter[] predicates = {
            new TagNameFilter(childTag),
            new HasAttributeFilter("class", tagClass)
    };
    NodeFilter filter = new AndFilter(predicates);
    NodeList matches = parents.extractAllNodesThatMatch(filter, true);
    return matches.size();
  }

  public String valueOfTagWithIdIs(String id) throws Exception {
    return getValueOfTagWithAttributeValue("id", id);
  }

  private String getValueOfTagWithAttributeValue(String attribute, String value) throws Exception {
    NodeList matches = getMatchingTags(new HasAttributeFilter(attribute, value));
    if (matches.size() != 1)
      return String.format("There are %d matches, there should be 1.", matches.size());
    else
      return matches.elementAt(0).toHtml();
  }

  public String valueOfTagWithClassIs(String classValue) throws Exception {
    return getValueOfTagWithAttributeValue("class", classValue);
  }

  public boolean contentOfTagWithIdContains(String id, String contents) throws Exception {
    String html = getValueOfTagWithAttributeValue("id", id);
    return (html.contains(contents));
  }

  public String contentOfTagWithId(String id) throws Exception {
    return getValueOfTagWithAttributeValue("id", id);
  }


  private static class HasAttributePrefixFilter extends HasAttributeFilter {
    private static final long serialVersionUID = 1L;

    public HasAttributePrefixFilter(String attribute, String prefix) {
      super(attribute, prefix);
    }

    @Override
    public boolean accept(Node node) {
      if (!(node instanceof Tag)) {
        return false;
      }

      if (mValue == null) {
        return false;
      }

      Tag tag = (Tag) node;
      if (tag.getAttributeEx(mAttribute) == null) {
        return false;
      }

      return tag.getAttributeEx(mAttribute).getValue().startsWith(mValue);
    }
  }

  public boolean pageHasAttribute(String fullPathOfPage, String attribute) throws Exception {
    PageCrawler crawler = FitnesseFixtureContext.context.getRootPage().getPageCrawler();
    WikiPage page = crawler.getPage(PathParser.parse(fullPathOfPage));
    PageData data = page.getData();
    return data.hasAttribute(attribute);
  }
}
