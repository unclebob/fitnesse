// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MockXmlizerPageHandler implements XmlizerPageHandler {
  public List<String> handledPages = new LinkedList<String>();
  public List<Date> modDates = new LinkedList<Date>();
  public int exits = 0;

  public void enterChildPage(WikiPage newPage, Date lastModified) throws Exception {
    handledPages.add(newPage.getName());
    modDates.add(lastModified);
  }

  public void exitPage() {
    exits++;
  }
}
