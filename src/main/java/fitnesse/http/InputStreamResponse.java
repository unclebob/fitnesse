// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import util.StreamReader;

public class InputStreamResponse extends Response {
  private StreamReader reader;
  private int contentSize = 0;

  public InputStreamResponse() {
    super("html");
  }

  public void sendTo(ResponseSender sender) throws IOException {
    try {
      sender.send(makeHttpHeaders().getBytes());
      while (!reader.isEof())
        sender.send(reader.readBytes(1000));
    } finally {
      reader.close();
      sender.close();
    }
  }

  @Override
  protected void addContentHeaders() {
    super.addContentHeaders();
    addHeader("Content-Length", getContentSize() + "");
  }

  public int getContentSize() {
    return contentSize;
  }

  public void setBody(InputStream input, int size) {
    reader = new StreamReader(input);
    contentSize = size;
  }

  public void setBody(File file) throws FileNotFoundException {
    FileInputStream input;
    input = new FileInputStream(file);
    int size = (int) file.length();
    setBody(input, size);
  }
}
