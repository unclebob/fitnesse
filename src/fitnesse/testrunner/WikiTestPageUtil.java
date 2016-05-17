package fitnesse.testrunner;

import fitnesse.testsystems.TestPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPageUtil;

public class WikiTestPageUtil {

  public static String makePageHtml(WikiTestPage page){
    return WikiPageUtil.getHeaderPageHtml(page.getSourcePage()) + page.getHtml();
  }

  public static WikiPage getSourcePage(TestPage testPage) {
    if (testPage instanceof WikiTestPage) {
      return ((WikiTestPage) testPage).getSourcePage();
    }
    return new WikiPageDummy();
  }
}
