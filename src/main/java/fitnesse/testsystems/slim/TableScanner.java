// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.Iterator;

public interface TableScanner<T extends Table> extends Iterable<T> {
  public int getTableCount();

  public T getTable(int i);

  public Iterator<T> iterator();
}
