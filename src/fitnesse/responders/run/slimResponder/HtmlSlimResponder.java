package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.PageData;

public class HtmlSlimResponder extends SlimResponder {
  protected SlimTestSystem getTestSystem(PageData pageData) {
    return new HtmlSlimTestSystem(pageData.getWikiPage(), this);
  }
}
