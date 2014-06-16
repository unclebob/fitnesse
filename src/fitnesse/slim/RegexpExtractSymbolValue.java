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

  public String getValue(int row, int col) {
    Object value = data.get(row).get(col);
    if(value==null) value="";
    Matcher m = Pattern.compile(regexp).matcher(value.toString());
    return m.matches() ? m.group(1) : "";
  }
}

