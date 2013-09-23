// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import static util.ListUtility.list;

import java.util.List;

public class TestTable {
  private String param = "";

  public TestTable(String x) {
    param = x;
  }

  public TestTable() {
  }

  @SuppressWarnings("unchecked")
  public List<?> doTable(List<?> l) {
    List<String> row0 = (List<String>) l.get(0);
    String firstCell = row0.get(0);
    return list(
      list("pass", "error:huh", param),
      list("bill", "no change", "pass:jake"),
      list("pass:<img src=http://localhost:8080/files/images/stop.gif/>", "pass:"+firstCell)
    );
  }
}
