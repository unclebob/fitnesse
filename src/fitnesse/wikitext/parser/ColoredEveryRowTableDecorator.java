package fitnesse.wikitext.parser;

/**
* @author Dmitrii Kartashov
*/
class ColoredEveryRowTableDecorator extends TableDecorator {

  @Override
  void decorateRow(HtmlWriter writer, String body) {
    byte[] bodyBytes = body.getBytes();
    int sum = 0;
    for (byte b : bodyBytes) {
      sum = sum + (int) b;
    }
    writer.putAttribute("class", "slimRowColor" + (sum % 10));
  }
}
