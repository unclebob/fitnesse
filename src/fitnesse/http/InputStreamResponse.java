// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import fitnesse.util.StreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class InputStreamResponse extends Response {
  private StreamReader reader;
  private int contentSize = 0;

  public InputStreamResponse() {
    super("html");
  }

  public void readyToSend(ResponseSender sender) throws Exception {
    try {
      addStandardHeaders();
      sender.send(makeHttpHeaders().getBytes());
      while (!reader.isEof())
        sender.send(reader.readBytes(1000));
    } finally {
      reader.close();
      sender.close();
    }
  }

  protected void addSpecificHeaders() {
    addHeader("Content-Length", getContentSize() + "");
  }

  public int getContentSize() {
    return contentSize;
  }

  public void setBody(InputStream input, int size) {
    reader = new StreamReader(input);
    contentSize = size;
  }

  public void setBody(File file) throws Exception {
    FileInputStream input = new FileInputStream(file);
    int size = (int) file.length();
    setBody(input, size);
  }
}
