// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import java.io.*;
import java.net.Socket;

public interface SocketServer {
  public void serve(Socket s);

  static class StreamUtility {
    public static PrintStream GetPrintStream(Socket s) throws IOException {
      OutputStream os = s.getOutputStream();
      return new PrintStream(os);
    }

    public static BufferedReader GetBufferedReader(Socket s) throws IOException {
      InputStream is = s.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      return new BufferedReader(isr);
    }
  }
}
