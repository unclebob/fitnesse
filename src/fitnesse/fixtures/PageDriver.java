// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.responders.run.XmlFormatter;
import fitnesse.wiki.*;
import org.htmlparser.*;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PageDriver {
  private PageCreator creator = new PageCreator();
  private ResponseRequester requester = new ResponseRequester();
  private ResponseExaminer examiner = new ResponseExaminer();

  public void createPageWithContent(String pageName, String content) throws Exception {
    creator.pageName = pageName;
    creator.pageContents = content;
    creator.valid();
  }

  public int requestPage(String uri) throws Exception {
    requester.uri = uri;
    requester.execute();
    return requester.status();
  }

  public void makeATestPage(String pageName) throws Exception {
    WikiPage root = FitnesseFixtureContext.root;
    WikiPagePath pagePath = PathParser.parse(pageName);
    WikiPage thePage = root.getPageCrawler().getPage(root, pagePath);
    PageData data = thePage.getData();
    data.setAttribute("Test", "true");
    thePage.commit(data);
  }

  public boolean contentMatches(String pattern) throws Exception {
    examiner.type = "contents";
    examiner.pattern = pattern;
    return examiner.matches();
  }

  public boolean contentContains(String subString) throws Exception {
    examiner.type = "contents";
    examiner.extractValueFromResponse();
    return examiner.getValue().indexOf(subString) != -1;
  }

  public boolean htmlContains(String subString) throws Exception {
    String html = requester.html();
    html = html.replaceAll("\n", " ");
    html = html.replaceAll("\r", " ");
    html = html.replaceAll("\\s+", " ");
    return (html.indexOf(subString) != -1);
  }

  public boolean containsJsonPacket(String packet) throws Exception {
    packet = ResponseExaminer.convertBreaksToLineSeparators(packet);
    System.out.println("packet = " + packet);
    JSONObject expected = new JSONObject(packet);
    String contentString = requester.contents();
    int jsonStart = contentString.indexOf("{");
    if (jsonStart == -1)
      return false;
    contentString = contentString.substring(jsonStart);
    System.out.println("contentString = " + contentString);
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
    String lines[] = priorToContent.split("\n");
    return lines.length;
  }

  public String echo(String it) {
    return it;
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
    SimpleDateFormat dateFormat = new SimpleDateFormat(XmlFormatter.TEST_RESULT_FILE_DATE_PATTERN);
    return dateFormat.format(date);
  }

  public int countOfTagWithIdAndWithClassBelowTagWithIdPrefix(String childTag, String childId, String tagClass, String parentTag, String parentIdPrefix) throws Exception {
    NodeList parents = getMatchingTags(
        new AndFilter(
            new TagNameFilter(parentTag),
            new HasAttributePrefixFilter("id", parentIdPrefix))
    );

    NodeFilter predicates[] = {
        new TagNameFilter(childTag),
        new HasAttributeFilter("class", tagClass),
        new HasAttributeFilter("id", childId)
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
    return (html.indexOf(contents) != -1);
  }

  public String contentOfTagWithId(String id) throws Exception {
    return getValueOfTagWithAttributeValue("id", id);
  }


  private static class HasAttributePrefixFilter extends HasAttributeFilter {
    private static final long serialVersionUID = 1L;

    public HasAttributePrefixFilter(String attribute, String prefix) {
      super(attribute, prefix);
    }

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
    PageCrawler crawler = FitnesseFixtureContext.root.getPageCrawler();
    WikiPage page = crawler.getPage(FitnesseFixtureContext.root, PathParser.parse(fullPathOfPage));
    PageData data = page.getData();
    return data.hasAttribute(attribute);
  }

  public int echoInt(int i) {
    return i;
  }
}
