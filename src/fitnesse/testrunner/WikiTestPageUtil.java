package fitnesse.testrunner;

import fitnesse.http.Request;
import fitnesse.wiki.WikiPageUtil;

public class WikiTestPageUtil {
  public static String makePageHtml(WikiTestPage page){
        return makePageHtml(page, null);
  }

  public static String makePageHtml(WikiTestPage page, Request request) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(WikiPageUtil.getHeaderPageHtml(page.getSourcePage(), request));
    buffer.append(page.getHtml());
    return buffer.toString();
  }
}
