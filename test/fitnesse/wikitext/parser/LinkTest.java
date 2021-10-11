package fitnesse.wikitext.parser;

import org.junit.Test;

public class LinkTest {
  @Test
  public void scansLinks() {
    ParserTestHelper.assertScansTokenType("http://mysite.org", "Link", true);
    ParserTestHelper.assertScansTokenType("https://mysite.org", "Link", true);
    ParserTestHelper.assertScansTokenType("http:/mysite.org", "Link", false);
    ParserTestHelper.assertScansTokenType("httpx://mysite.org", "Link", false);
  }

  @Test
  public void parsesLinks() throws Exception {
    ParserTestHelper.assertParses("http://mysite.org", "SymbolList[Link[SymbolList[Text]]]");
  }

  @Test
  public void translatesLinks() {
    ParserTestHelper.assertTranslatesTo("http://mysite.org", "<a href=\"http://mysite.org\">http://mysite.org</a>");
    ParserTestHelper.assertTranslatesTo("http://files/myfile", "<a href=\"files/myfile\">http://files/myfile</a>");
    ParserTestHelper.assertTranslatesTo("''http://files/myfile''", "<i><a href=\"files/myfile\">http://files/myfile</a></i>");
  }

  @Test
  public void translatesLinkWithVariable() {
    ParserTestHelper.assertTranslatesTo("http://${site}", new TestVariableSource("site", "mysite.org"), "<a href=\"http://mysite.org\">http://mysite.org</a>");
  }

  @Test
  public void translatesImageLinks() {
    ParserTestHelper.assertTranslatesTo("http://some.jpg", "<img src=\"http://some.jpg\"/>");
    ParserTestHelper.assertTranslatesTo("http://files/myfile.jpg", "<img src=\"files/myfile.jpg\"/>");

    ParserTestHelper.assertTranslatesTo("http://files/myfile.gif", "<img src=\"files/myfile.gif\"/>");
    ParserTestHelper.assertTranslatesTo("http://files/myfile.png", "<img src=\"files/myfile.png\"/>");
    ParserTestHelper.assertTranslatesTo("http://files/myfile.svg", "<img src=\"files/myfile.svg\"/>");
  }
}
