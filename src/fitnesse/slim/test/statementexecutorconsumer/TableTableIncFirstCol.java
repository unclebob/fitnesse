package fitnesse.slim.test.statementexecutorconsumer;

import java.util.ArrayList;
import java.util.List;

public class TableTableIncFirstCol extends SymbolManagingTableTable {

  @Override
  protected List<List<String>> doTableImpl(List<List<?>> table) {
    List<List<String>> ret = new ArrayList<>();
    for (List<?> line : table) {
      List<String> retLine = new ArrayList<>();
      ret.add(retLine);

      retLine.add("no change");
      String oldValue = replaceSymbolsInString(line.get(0).toString());
      int newValue = Integer.parseInt(oldValue) + 1;
      assignSymbolIfApplicable(line.get(1).toString(), newValue);
      retLine.add("pass:" + newValue);
    }
    return ret;
  }
}
