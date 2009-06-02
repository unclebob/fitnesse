// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.util.Iterator;

public interface TableScanner extends Iterable<Table> {
  public int getTableCount();

  public Table getTable(int i);

  public Iterator<Table> iterator();

  public String toWikiText();

  public String toHtml(Table startAfterTable, Table endWithTable);
  
  public String toHtml();
}
