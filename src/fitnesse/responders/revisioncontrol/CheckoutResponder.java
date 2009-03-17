// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKOUT;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.FileSystemPage;

public class CheckoutResponder extends RevisionControlResponder {
  public CheckoutResponder() {
    super(CHECKOUT);
  }

  @Override
  protected String responseMessage(String resource) throws Exception {
    return "Click " + HtmlUtil.makeLink(resource + "?edit", "here").html() + " to edit the page.";
  }

  @Override
  protected void performOperation(FileSystemPage page) throws Exception {
    page.execute(CHECKOUT);
  }
}
