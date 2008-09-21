package fitnesse.responders.run;

import fitnesse.responders.WikiPageResponder;
import fitnesse.wiki.PageData;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;

public class SlimResponder extends WikiPageResponder {
  /* hook for subclasses */
  protected void processWikiPageDataBeforeGeneratingHtml(PageData pageData) throws Exception {
    String content = "!c !3 !style_pass(SLIM TEST)\n" + pageData.getContent();
    pageData.setContent(content);
  }

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }
}
