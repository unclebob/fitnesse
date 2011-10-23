package fitnesse.fixtures;

import java.util.LinkedList;
import java.util.List;

public class TableOrDecisionFixture {
  
  public String parsingAs() {
    return "Parsed as Decision fixture";
  }
  
  public List doTable(List<List<String>> table) {
    List<String> firstRow = new LinkedList<String>();
    firstRow.add("");
    firstRow.add("pass: Parsed as Table fixture");
    
    List returnList = new LinkedList();
    returnList.add(firstRow);
    return returnList;
  }
  
}
