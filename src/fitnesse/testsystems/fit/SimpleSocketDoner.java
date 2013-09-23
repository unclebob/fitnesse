// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.util.MockSocket;

public class SimpleSocketDoner implements SocketDoner {
  private static final Logger LOG = Logger.getLogger(SimpleSocketDoner.class.getName());

  public Socket socket;
  public boolean finished = false;

  public SimpleSocketDoner() {
    socket = new MockSocket("SimpleSocketDoner");
  }

  public SimpleSocketDoner(Socket socket) {
    this.socket = socket;
  }

  public Socket donateSocket() {
    return socket;
  }

  public void finishedWithSocket() {
    finished = true;
    try {
      socket.close();
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Failed to close socket", e);
    }
  }
}
