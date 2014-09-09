// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.List;

public class TableTableExtractSymbol {
  private List<List<String>> data;

  public TableTableExtractSymbol(List<List<String>> data) {
    this.data = data;
  }

  public String getValue(int row, int col) {
    String value = data.get(row).get(col);
    int pos = value.indexOf(":");
    return (pos > 0) ? value.substring(pos + 1) : value;
  }
}

