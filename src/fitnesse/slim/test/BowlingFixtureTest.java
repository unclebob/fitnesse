package fitnesse.slim.test;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.List;

import org.junit.Test;

public class BowlingFixtureTest {
  @Test
  public void testFixture() throws Exception {
    List<Object> scoreSheet = list(
      list("3","5","4","/","X"," ","X"," ","3","4","6","/","7","2","3","4","9","-","4","/","3"),
      list("","8","","28","","51","","68","","75","","92","","101","","108","","117","","","127")
    );
    Bowling b = new Bowling();
    List<?> results = b.doTable(scoreSheet);
    List<?> rollResults = list("","","","","","","","","","","","","","","","","","","","","");
    List<?> scoreResults = list("","pass","","pass","","pass","","pass","","pass","","pass","","pass","","pass","","pass","","","pass");
    assertEquals(rollResults, results.get(0));
    assertEquals(scoreResults, results.get(1));
  }
}
