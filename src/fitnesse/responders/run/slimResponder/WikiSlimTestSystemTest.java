package fitnesse.responders.run.slimResponder;

import fitnesse.wiki.PageData;

public class WikiSlimTestSystemTest extends SlimTestSystemTest {
  protected SlimResponder getSlimResponder() {
    return new WikiSlimResponder();
  }
}
