package fitnesse.wikitext.parser;

import fitnesse.wikitext.parser.HtmlWriter;

/**
* @author Dmitrii Kartashov
*/
abstract class TableDecorator {
  abstract void decorateRow(HtmlWriter writer, String body);

  void onHeaderStarts(HtmlWriter writer) {
  }

  void onHeaderEnds(HtmlWriter writer) {
  }

  void decorateHeaderRow(HtmlWriter writer) {
    writer.putAttribute("class", "slimRowTitle");
  }

  public String getClassForTable() {
    return null;
  }
}
