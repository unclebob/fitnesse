package fitnesse.wikitext.parser;

/**
* @author Dmitrii Kartashov
*/
class DataTableDecorator extends TableDecorator {
  @Override
  void decorateRow(HtmlWriter writer, String body) {

  }

  @Override
  void onHeaderStarts(HtmlWriter writer) {
    writer.startTag("thead");
  }

  @Override
  void onHeaderEnds(HtmlWriter writer) {
    writer.endTag();
  }

  @Override
  void decorateHeaderRow(HtmlWriter writer) {

  }

  @Override
  public String getClassForTable() {
    return "display compact order-column stripe";
  }
}
