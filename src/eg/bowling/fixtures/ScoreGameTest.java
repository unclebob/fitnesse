package eg.bowling.fixtures;

import org.junit.Assert;
import org.junit.Test;

import fit.Parse;

public class ScoreGameTest {
  @Test
  public void test() throws Exception {
    ScoreGame sg = new ScoreGame();
    Parse table = new Parse(
      "<table>" +
        "<tr><td>ScoreGame</td></tr>" +
        "<tr>" +
        "<td>1-1</td>\n" +
        "<td>1-2</td>\n" +
        "<td>2-1</td>\n" +
        "<td>2-2</td>\n" +
        "<td>3-1</td>\n" +
        "<td>3-2</td>\n" +
        "<td>4-1</td>\n" +
        "<td>4-2</td>\n" +
        "<td>5-1</td>\n" +
        "<td>5-2</td>\n" +
        "<td>6-1</td>\n" +
        "<td>6-2</td>\n" +
        "<td>7-1</td>\n" +
        "<td>7-2</td>\n" +
        "<td>8-1</td>\n" +
        "<td>8-2</td>\n" +
        "<td>9-1</td>\n" +
        "<td>9-2</td>\n" +
        "<td>10-1</td>\n" +
        "<td>10-2</td>\n" +
        "<td>10-3</td>\n" +
        "</tr>\n" +
        "<tr>" +
        "<td>1</td>\n" +
        "<td>4</td>\n" +
        "<td>3</td>\n" +
        "<td>/</td>\n" +
        "<td>5</td>\n" +
        "<td>2</td>\n" +
        "<td></td>\n" +
        "<td>X</td>\n" +
        "<td></td>\n" +
        "<td>X</td>\n" +
        "<td>5</td>\n" +
        "<td>/</td>\n" +
        "<td>3</td>\n" +
        "<td>2</td>\n" +
        "<td>1</td>\n" +
        "<td>0</td>\n" +
        "<td>7</td>\n" +
        "<td>1</td>\n" +
        "<td>5</td>\n" +
        "<td>/</td>\n" +
        "<td>3</td>\n" +
        "</tr>\n" +
        "<tr>" +
        "<td></td>\n" +
        "<td>5</td>\n" +
        "<td></td>\n" +
        "<td>20</td>\n" +
        "<td></td>\n" +
        "<td>27</td>\n" +
        "<td></td>\n" +
        "<td>52</td>\n" +
        "<td></td>\n" +
        "<td>72</td>\n" +
        "<td></td>\n" +
        "<td>85</td>\n" +
        "<td></td>\n" +
        "<td>90</td>\n" +
        "<td></td>\n" +
        "<td>91</td>\n" +
        "<td></td>\n" +
        "<td>99</td>\n" +
        "<td></td>\n" +
        "<td>112</td>\n" +
        "<td></td>\n" +
        "</tr>" +
        "</table>");
    sg.doTable(table);
    Assert.assertEquals("10 right, 0 wrong, 0 ignored, 0 exceptions",sg.counts());
  }
}
