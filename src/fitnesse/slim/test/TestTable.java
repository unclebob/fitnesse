package fitnesse.slim.test;

import fitnesse.util.ListUtility;
import static fitnesse.util.ListUtility.*;

import java.util.List;

public class TestTable {
  public List doTable(List l) {
    return list(
      list("pass", "error:huh", ""),
      list("bill", "no change", "jake")
    );
  }
}
