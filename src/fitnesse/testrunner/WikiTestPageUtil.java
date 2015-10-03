package fitnesse.testrunner;

import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPageUtil;

public class WikiTestPageUtil {

  public static String makePageHtml(WikiTestPage page){
    StringBuilder buffer = new StringBuilder();
    buffer.append(WikiPageUtil.getHeaderPageHtml(page.getSourcePage()));
    buffer.append(page.getHtml());
    return buffer.toString();
  }

  public static WikiPage getSourcePage(TestPage testPage) {
    if (testPage instanceof WikiTestPage) {
      return ((WikiTestPage) testPage).getSourcePage();
    }
    return new WikiPageDummy();
  }
}
