// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.responders.run.XmlFormatter;
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
    return requester.contents();
  }

  public String lineIs(int lineNumber) throws Exception {
    examiner.type = "line";
    examiner.number = lineNumber;
    return examiner.string();
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

  private static class HasAttributePrefixFilter extends HasAttributeFilter {
    public HasAttributePrefixFilter(String attribute, String prefix) {
      super(attribute, prefix);
    }

    public boolean accept(Node node) {
      Tag tag;
      Attribute attribute;
      boolean ret;

      ret = false;
      if (node instanceof Tag) {
        tag = (Tag) node;
        attribute = tag.getAttributeEx(mAttribute);
        ret = null != attribute;
        if (ret && (null != mValue))
          ret = attribute.getValue().startsWith(mValue);
      }

      return (ret);
    }
  }
}
