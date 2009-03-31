// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.ArrayList;
import java.util.List;

public class NullVirtualCouplingPage extends VirtualCouplingPage {
  private static final long serialVersionUID = 1L;

  public NullVirtualCouplingPage(WikiPage hostPage) {
    super(hostPage);
  }

  public List<WikiPage> getChildren() {
    return new ArrayList<WikiPage>();
  }
}
