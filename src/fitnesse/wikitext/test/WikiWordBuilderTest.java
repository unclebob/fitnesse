package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.WikiWordBuilder;
import org.junit.Assert;
import org.junit.Test;

public class WikiWordBuilderTest {
  @Test public void buildsLinkToNonExistent() {
    WikiWordBuilder builder = new WikiWordBuilder(new TestSourcePage().withUrl("NonExistentPage"), "", "");
    String result = builder.buildLink("", "<i>name</i>");
    Assert.assertEquals("<i>name</i><a title=\"create page\" href=\"NonExistentPage?edit&amp;nonExistent=true\">[?]</a>", result);
  }
}
