package fitnesse.wikitext.parser;

/**
* @author Dmitrii Kartashov
*/
class ColoredTableDecorator extends TableDecorator
{
  int rowCount;

  @Override
  void decorateRow(HtmlWriter writer, String body) {
    writer.putAttribute("class", "slimRowColor" + (rowCount++ % 2));
  }
}
