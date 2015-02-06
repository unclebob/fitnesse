package fitnesse.slim.test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class BowlingFixtureTest {
  @Test
  public void testFixture() throws Exception {
    List<List<String>> scoreSheet = asList(asList("3", "5", "4", "/", "X", " ", "X", " ", "3", "4", "6", "/", "7", "2", "3", "4", "9", "-", "4", "/", "3"), asList("", "8", "", "28", "", "51", "", "68", "", "75", "", "92", "", "101", "", "108", "", "117", "", "", "127"));
    Bowling b = new Bowling();
    List<?> results = b.doTable(scoreSheet);
    List<?> rollResults = asList("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    List<?> scoreResults = asList("", "pass", "", "pass", "", "pass", "", "pass", "", "pass", "", "pass", "", "pass", "", "pass", "", "pass", "", "", "pass");
    assertEquals(rollResults, results.get(0));
    assertEquals(scoreResults, results.get(1));
  }
}
