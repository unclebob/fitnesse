// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.Closeable;
import java.io.IOException;

public interface ResponseSender extends Closeable {
  void send(byte[] bytes) throws IOException;

  default void sendLine(String line) throws IOException {
    send(line.getBytes());
    send(System.lineSeparator().getBytes());
  }

}
