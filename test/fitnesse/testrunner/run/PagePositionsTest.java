package fitnesse.testrunner.run;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class PagePositionsTest {
  private static final String CONTENT =
    "Page\tPartition\tTest System\tOrder\n" +
      "FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.AlwaysSecureOperation\t4\tslim\t0\n" +
      "FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.SecureReadOperations\t1\tslim\t1\n" +
      "FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.SecureTestOperations\t1\tslim\t0\n";

  @Test
  public void roundTrip() throws IOException {
    try (BufferedReader r = new BufferedReader(new StringReader(CONTENT))) {
      PagePositions pagePositions = PagePositions.parseFrom(r, "\t");

      String written = pagePositions.toString();

      assertEquals(CONTENT, written);

      assertEquals(asList("Partition", "Test System"), pagePositions.getGroupNames());
      assertEquals(Integer.valueOf(1), pagePositions.getGroupIndex("Test System"));

      assertEquals(
        asList("FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.AlwaysSecureOperation",
          "FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.SecureReadOperations",
          "FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.SecureTestOperations"),
        pagePositions.getPages());
    }
  }

  @Test
  public void testFormatWikiPageIdentity() {
    WikiPage p = new WikiPageDummy("a", "a", null);

    PagePositions poss = new PagePositions();
    WikiPageIdentity identity = new WikiPageIdentity(p);
    assertEquals("fit", poss.formatDimension(identity));
    assertEquals("1", poss.formatDimension(1));
    assertEquals("b", poss.formatDimension("b"));
    assertEquals("null", poss.formatDimension(null));
  }

  @Test
  public void createByPositionInGroupComparator() throws IOException {
    try (BufferedReader r = new BufferedReader(new StringReader(CONTENT))) {
      PagePositions pagePositions = PagePositions.parseFrom(r, "\t");
      Comparator<String> comparator = pagePositions.createByPositionInGroupComparator();

      List<String> pageNames = new ArrayList<>(pagePositions.getPages());
      pageNames.add("FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.Uknown");
      pageNames.sort(comparator);

      assertEquals(0, pageNames.indexOf("FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.Uknown"));
      assertEquals(1, pageNames.indexOf("FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.AlwaysSecureOperation"));
      assertEquals(2, pageNames.indexOf("FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.SecureTestOperations"));
      assertEquals(3, pageNames.indexOf("FitNesse.SuiteAcceptanceTests.SuiteAuthenticationTests.SecureReadOperations"));
    }
  }
}
