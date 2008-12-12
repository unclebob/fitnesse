package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.PageData;
import fitnesse.responders.run.TestSystemListener;

public class HtmlSlimTestSystem extends SlimTestSystem {
  public HtmlSlimTestSystem(WikiPage page, TestSystemListener listener) {
    super(page, listener);
  }

  protected TableScanner scanTheTables(PageData pageData) throws Exception {
    return new HtmlTableScanner(pageData.getHtml());
  }

  protected String createHtmlResults() throws Exception {
    replaceExceptionsWithLinks();
    evaluateTables();
    return ExceptionList.toHtml(exceptions)+ tableScanner.toHtml();
  }
}
