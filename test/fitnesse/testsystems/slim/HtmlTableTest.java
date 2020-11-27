package fitnesse.testsystems.slim;

import fitnesse.testsystems.slim.results.SlimTestResult;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.junit.Test;

import java.util.Collections;

import static fitnesse.testsystems.slim.HtmlTable.qualifiesAsConvertedList;
import static fitnesse.testsystems.slim.HtmlTable.qualifiesAsHtml;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HtmlTableTest {

  private final String listContainingHashTable = "$list<-[[a, b, <table class=\"hash_table\">\n" +
    "<tr class=\"hash_row\">\n" +
    "<td class=\"hash_key\">first</td>\n" +
    "<td class=\"hash_value\">1</td>\n" +
    "</tr>\n" +
    "<tr class=\"hash_row\">\n" +
    "<td class=\"hash_key\">second</td>\n" +
    "<td class=\"hash_value\">2</td>\n" +
    "</tr>\n" +
    "</table>]]";

  private final String listContainingImage = "$list<-[[a, b, <img src='http://fitnesse.org'/>]]";

  @Test
  public void normalTextDoesNotQualifyAsHtml() {
    assertFalse(qualifiesAsHtml("Some text"));
  }

  @Test
  public void textWithLessThanDoesNotQualifiesAsHtml() {
    assertFalse(qualifiesAsHtml("less < more"));
  }

  @Test
  public void htmlTableQualifies() {
    assertTrue(qualifiesAsHtml("<table><tr><td>blah etc.</td></tr></table>"));
  }

  @Test
  public void multiLineHtmlShouldQualify() {
    assertTrue(qualifiesAsHtml("<table>\n" +
            "\t<tr>\n" +
            "\t\t<td>table2</td>\n" +
            "\t</tr>\n" +
            "\t<tr>\n" +
            "\t\t<td>value2</td>\n" +
            "\t</tr>\n" +
            "</table>"));
  }

  @Test
  public void htmlImageQualifies() {
    assertTrue(qualifiesAsHtml("<img src='http://fitnesse.org'/>"));
    assertTrue(qualifiesAsHtml("<img src='http://fitnesse.org' />"));
  }

  @Test
  public void htmlTableAndTextDoesNotQualify() {
    assertFalse(qualifiesAsHtml("<table><tr><td>blah etc.</td></tr></table>And extra text"));
  }

  @Test
  public void htmlDivWithUnescapedLessThanDoesQualify() {
    assertTrue(qualifiesAsHtml("<div>less < more</div>"));
    assertTrue(qualifiesAsHtml("<div>less < more > less</div>"));
  }

  @Test
  public void htmlFromSymbolReplacementDoesQualify() {
    assertTrue(qualifiesAsHtml("$foo->[<div>less < more</div>]"));
  }

  @Test
  public void justSomeXmlDoesNotQualify() {
    assertFalse(qualifiesAsHtml("<content>blah</content>"));
  }

  @Test
  public void htmlDocumentDoesNotQualify() {
    assertFalse(qualifiesAsHtml("<html><head></head><body></body></html>"));
  }

  @Test
  public void convertedListQualifiesAsConvertedList() {
    assertTrue(qualifiesAsConvertedList("[a, b, c]"));
    assertTrue(qualifiesAsConvertedList(listContainingHashTable));
  }

  @Test
  public void symbolAssignmentOfConvertedListQualifiesAsConvertedList() {
    assertTrue(qualifiesAsConvertedList("$list<-[[a, b]]"));
    assertTrue(qualifiesAsConvertedList(listContainingHashTable));
  }

  @Test
  public void convertedListContainingHashTableIsNotEscaped() {
    HtmlTable t = getDummyTable();
    t.addRow(Collections.singletonList(listContainingHashTable));
    assertFalse(t.toHtml().contains("&lt;"));
    assertTrue(t.toHtml().contains(listContainingHashTable));
  }

  @Test
  public void convertedListContainingOtherHtmlIsEscaped() {
    HtmlTable t = getDummyTable();
    t.addRow(Collections.singletonList(listContainingImage));
    assertTrue(t.toHtml().contains("&lt;"));
    assertFalse(t.toHtml().contains(listContainingHashTable));
  }

  @Test
  public void ignoredCellWithoutMessageShouldRenderOriginalContentAsIgnored() {
    HtmlTable.Cell cell = new HtmlTable.Cell("original content");

    cell.setTestResult(SlimTestResult.ignore());

    assertThat(cell.formatTestResult(), is("<span class=\"ignore\">original content</span>"));
  }

  @Test
  public void ignoredCellWithMessageShouldRenderMessageAsIgnored() {
    HtmlTable.Cell cell = new HtmlTable.Cell("original content");

    cell.setTestResult(SlimTestResult.ignore("a message"));

    assertThat(cell.formatTestResult(), is("<span class=\"ignore\">a message</span>"));
  }

  private HtmlTable getDummyTable() {
    TableTag tbl = new TableTag();
    TableRow row = new TableRow();
    row.setChildren(new NodeList(new TableColumn()));
    tbl.setChildren(new NodeList(row));
    return new HtmlTable(tbl);
  }

}
