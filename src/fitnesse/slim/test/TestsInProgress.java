// Copyright (C) 2010 by Konstantin Vlasenko
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

import static util.ListUtility.list;
import java.io.File;
import java.util.List;

public class TestsInProgress {
  private String[] lines;

  public List<Object> query() {
    List<Object> table = list();
	File folder = new File("FitNesseRoot/files/testProgress/");
    File[] listOfFiles = folder.listFiles();

    for (int i = 1; i < listOfFiles.length; i++) {
		List<String> test = list("Test", listOfFiles[i].getName());
		List<Object> row = list(test);
        table.add(row);
    }
	return table;
  }
}