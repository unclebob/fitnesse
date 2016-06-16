// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.IOException;
import java.util.Date;

public interface XmlizerPageHandler {
  void enterChildPage(WikiPage newPage, Date lastModified) throws IOException;

  void exitPage();
}
