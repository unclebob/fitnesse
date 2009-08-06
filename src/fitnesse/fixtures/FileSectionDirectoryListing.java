// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileSectionDirectoryListing {

  public List<Object> query() throws Exception {
    File[] files = FileSection.getFileSection().listFiles();
    List<Object> rows = new ArrayList<Object>();
    for (File file : files) {
      List<Object> row = new ArrayList<Object>();
      int substringLength = FileSection.getFileSection().getPath().length();
      row.add(Arrays.asList("path", file.getPath().substring(substringLength +1)));
      rows.add(row);
    }
    return rows;
  }
}
