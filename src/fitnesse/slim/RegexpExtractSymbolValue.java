// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpExtractSymbolValue {
  private List<List<Object>> data;
  private String regexp;

  public RegexpExtractSymbolValue(List<List<Object>> data, String regexp) {
    this.data = data;
    this.regexp = regexp;
  }

  public Object getValue(int row, int col) {
    if (row < data.size()) {
      List<Object> dataRow = data.get(row);
      if (col < dataRow.size()) {
        Object value = dataRow.get(col);
        if (value != null) {
          String valueStr = value.toString();
          if (valueStr.length() > 0) {
            Matcher m = Pattern.compile(regexp).matcher(valueStr);
            return m.matches() ? m.group(1) : value;
          }
        }
      }
    }
    return "";
  }
}

