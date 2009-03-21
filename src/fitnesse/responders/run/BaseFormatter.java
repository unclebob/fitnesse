// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.WikiPage;

public abstract class BaseFormatter implements ResultsListener {

  private final WikiPage page;

  public abstract void writeHead(String pageType) throws Exception;

  public abstract void allTestingComplete() throws Exception;

  protected BaseFormatter(final WikiPage page) {
    this.page = page;
  }
  
  protected WikiPage getPage() {
    return page;
  }
  
  public void errorOccured() {
    try {
      allTestingComplete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
