package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.PageData;

public class HtmlSlimTestSystemTest extends SlimTestSystemTest {
  protected SlimResponder getSlimResponder() {
    return new HtmlSlimResponder();
  }
}
