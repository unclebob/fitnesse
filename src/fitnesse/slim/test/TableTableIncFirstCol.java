package fitnesse.slim.test;

import java.util.ArrayList;
import java.util.List;

public class TableTableIncFirstCol {

  public List<List<String>> doTable(List<List<?>> table) {
    List<List<String>> ret = new ArrayList<>();

    for (List<?> line : table) {
      List<String> retLine = new ArrayList<>();
      ret.add(retLine);

      retLine.add("no change");
      retLine.add("pass:" + (Integer.parseInt(line.get(0).toString()) + 1));
    }

    return ret;
  }
}
