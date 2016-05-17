// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.socketservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public interface SocketServer {

  void serve(Socket s) throws IOException;

  class StreamUtility {
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
