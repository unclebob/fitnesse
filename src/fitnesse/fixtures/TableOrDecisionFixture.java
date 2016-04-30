package fitnesse.fixtures;

import java.util.LinkedList;
import java.util.List;

public class TableOrDecisionFixture {

  public String parsingAs() {
    return "Parsed as Decision fixture";
  }

  public List<List<String>> doTable(List<List<String>> table) {
    List<String> firstRow = new LinkedList<>();
    firstRow.add("");
    firstRow.add("pass: Parsed as Table fixture");

    List<List<String>> returnList = new LinkedList<>();
    returnList.add(firstRow);
    return returnList;
  }

}
