// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.net.Socket;

public interface SocketDoner {
  public Socket donateSocket();

  public void finishedWithSocket() throws Exception;
}
