// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import fitnesse.http.Response;
import fitnesse.http.ResponseSender;

public class PuppetResponse extends Response {
  private ResponsePuppeteer puppeteer;

  public PuppetResponse(ResponsePuppeteer puppeteer) {
    super("html");
    this.puppeteer = puppeteer;
  }

  public void sendTo(ResponseSender sender) {
    puppeteer.readyToSend(sender);
  }

  public int getContentSize() {
    return 0;
  }
}
