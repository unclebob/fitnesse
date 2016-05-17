package fitnesse.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplitFixture {
  private String[] lines;

  public SplitFixture(String schema) {
    lines = schema.split(";");
  }

  public List<Object> query() {
    List<Object> table = new ArrayList<>();
    for (String lineContent : lines) {
      List<Object> line = new ArrayList<>();
      String[] words = lineContent.split(",");
      for (int i = 0; i < words.length; i++) {
        String word = words[i];
        line.add(Arrays.asList(Integer.toString(i+1), word));
      }
      table.add(line);
    }
    return table;
  }
}
