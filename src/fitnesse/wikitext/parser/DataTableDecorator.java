package fitnesse.wikitext.parser;

/**
* @author Dmitrii Kartashov
*/
class DataTableDecorator extends TableDecorator {
  @Override
  void decorateRow(HtmlWriter writer, String body) {

  }

  @Override
  void decorateHeaderRow(HtmlWriter writer) {

  }

  @Override
  public String getClassForTable() {
    return "display compact";
  }
}
