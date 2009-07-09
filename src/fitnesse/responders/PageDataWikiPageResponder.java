// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.wiki.WikiPage;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;

public class PageDataWikiPageResponder extends BasicWikiPageResponder {
  protected String contentFrom(WikiPage requestedPage)
    throws Exception {
    return requestedPage.getData().getContent();
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
