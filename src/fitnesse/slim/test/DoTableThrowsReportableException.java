package fitnesse.slim.test;

import java.util.List;

public class DoTableThrowsReportableException {
  public List<Object> doTable(List<List<String>> table) {
    throw new RuntimeException("A Reportable Exception");
  }
}
