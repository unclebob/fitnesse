package fitnesse.slim.test.statementexecutorconsumer;

import java.util.ArrayList;
import java.util.List;

public class TableTableIncFirstCol extends SymbolManagingTableTable {

  @Override
  protected List<List<String>> doTableImpl(List<List<String>> table) {
    List<List<String>> ret = new ArrayList<>();
    for (List<String> line : table) {
      List<String> retLine = new ArrayList<>();
      ret.add(retLine);

      retLine.add("no change");
      String oldValue = replaceSymbolsInString(line.get(0));
      int newValue = Integer.parseInt(oldValue) + 1;
      assignSymbolIfApplicable(line.get(1), newValue);
      retLine.add("pass:" + newValue);
    }
    return ret;
  }
}
