// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.net.Socket;

public interface ResponseSender {
  public void send(byte[] bytes);

  public void close();

  public Socket getSocket(); //TODO-MdM maybe get rid of this method.
}
