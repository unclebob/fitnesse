// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.http.ResponseSender;

public interface ResponsePuppeteer {
  void readyToSend(ResponseSender sender) throws Exception;
}
