package fitnesse.wikitext.parser;

/**
* @author Dmitrii Kartashov
*/
abstract class TableDecorator {
  abstract void decorateRow(HtmlWriter writer, String body);

  void decorateHeaderRow(HtmlWriter writer) {
    writer.putAttribute("class", "slimRowTitle");
  }

  public String getClassForTable() {
    return null;
  }
}
