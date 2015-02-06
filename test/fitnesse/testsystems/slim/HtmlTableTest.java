package fitnesse.testsystems.slim;

import fitnesse.testsystems.slim.results.SlimTestResult;
import org.junit.Test;

import static fitnesse.testsystems.slim.HtmlTable.qualifiesAsHtml;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HtmlTableTest {

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

}
