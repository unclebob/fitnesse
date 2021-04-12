package fitnesse.wikitext.parser;

import org.junit.Assert;
import org.junit.Test;

public class WikiWordBuilderTest {
  @Test
  public void buildsLinkToNonExistent() {
    WikiWordBuilder builder = new WikiWordBuilder(new TestSourcePage().withTarget("NonExistentPage"), "", "");
    String result = builder.buildLink("", "<i>name</i>");
    Assert.assertEquals("<i>name</i><a title=\"create page\" href=\"NonExistentPage?edit&amp;nonExistent=true\">[?]</a>", result);
  }

  @Test
  public void formatFitNesseName() {
    WikiWordBuilder builder = new WikiWordBuilder(new TestSourcePage().withTarget("NonExistentPage"), "", "");
    String result = builder.buildLink("", "FitNesse");
    Assert.assertEquals("<span class=\"fitnesse\">FitNesse</span>", result);
  }
}
