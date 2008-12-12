package fitnesse.responders.run.slimResponder;

import fitnesse.responders.run.TestSystemListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class WikiSlimTestSystem extends SlimTestSystem {
  public WikiSlimTestSystem(WikiPage page, TestSystemListener listener) {
    super(page, listener);
  }

  protected TableScanner scanTheTables(PageData pageData) throws Exception {
    return new WikiTableScanner(pageData);
  }

  protected String createHtmlResults() throws Exception {
    String wikiText = generateWikiTextForTestResults();
    testResults.setContent(wikiText);
    String html = testResults.getHtml();
    return html;
  }

  private String generateWikiTextForTestResults() throws Exception {
    replaceExceptionsWithLinks();
    evaluateTables();
    return ExceptionList.toHtml(exceptions) + testResultsToWikiText();
  }

  private String testResultsToWikiText() throws Exception {
    String wikiText = tableScanner.toWikiText();

    return wikiText;
  }

}
