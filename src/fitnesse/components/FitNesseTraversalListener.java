// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.WikiPage;

//TODO rename me to TraversalListener
public interface FitNesseTraversalListener {
  public void processPage(WikiPage page) throws Exception;

  public String getSearchPattern() throws Exception;
}
