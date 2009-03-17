// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.net.Socket;

import fitnesse.responders.run.SocketDoner;
import fitnesse.responders.run.SocketSeeker;

public class SimpleSocketSeeker implements SocketSeeker {
  public SocketDoner doner;
  public Socket socket;

  public void acceptSocketFrom(SocketDoner doner) throws Exception {
    this.doner = doner;
    this.socket = doner.donateSocket();
  }
}
