// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.wiki.WikiPage;

public class PageDataWikiPageResponder extends BasicWikiPageResponder {
  protected String contentFrom(WikiPage requestedPage)
    throws Exception {
    return requestedPage.getData().getContent();
  }

}
