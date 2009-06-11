// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.WikiPage;

public interface TraversalListener {
  public void processPage(WikiPage page) throws Exception;
}
