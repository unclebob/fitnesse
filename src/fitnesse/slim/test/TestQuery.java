// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestQuery {
  private int n;

  public TestQuery(int n) {
    this.n = n;
  }

  public List<Object> query() {
    List<Object> table = new ArrayList<>();
    for (int i = 1; i <= n; i++) {
      List<String> ncol = Arrays.asList("n", String.valueOf(i));
      List<String> n2col = Arrays.asList("2n", String.valueOf(2 * i));
      List<List<String>> row = Arrays.asList(ncol, n2col);
      table.add(row);
    }
    return table;
  }
}
