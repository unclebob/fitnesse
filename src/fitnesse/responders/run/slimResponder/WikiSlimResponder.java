package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.PageData;

public class WikiSlimResponder extends SlimResponder {
  protected SlimTestSystem getTestSystem(PageData pageData) {
    return new WikiSlimTestSystem(pageData.getWikiPage(), this);
  }
}
