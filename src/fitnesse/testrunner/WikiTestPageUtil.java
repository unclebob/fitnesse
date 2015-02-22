package fitnesse.testrunner;

import fitnesse.wiki.WikiPageUtil;

public class WikiTestPageUtil {
  public static String makePageHtml(WikiTestPage page){
    StringBuffer buffer = new StringBuffer();
    buffer.append(WikiPageUtil.getHeaderPageHtml(page.getSourcePage()));
    buffer.append(page.getHtml());
    return buffer.toString();
  }
}
