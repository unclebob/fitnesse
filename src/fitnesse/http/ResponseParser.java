// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.StreamReader;

public class ResponseParser {
  private int status;
  private String body;
  private HashMap<String, String> headers = new HashMap<String, String>();
  private StreamReader input;

  private static final Pattern statusLinePattern = Pattern.compile("HTTP/\\d.\\d (\\d\\d\\d) ");
  private static final Pattern headerPattern = Pattern.compile("([^:]*): (.*)");

  public ResponseParser(InputStream input) throws Exception {
    this.input = new StreamReader(input);
    parseStatusLine();
    parseHeaders();
    if (isChuncked()) {
      parseChunks();
      parseHeaders();
    } else
      parseBody();
  }

  private boolean isChuncked() {
    String encoding = getHeader("Transfer-Encoding");
    return encoding != null && "chunked".equals(encoding.toLowerCase());
  }

  private void parseStatusLine() throws Exception {
    String statusLine = input.readLine();
    Matcher match = statusLinePattern.matcher(statusLine);
    if (match.find()) {
      String status = match.group(1);
      this.status = Integer.parseInt(status);
    } else
      throw new Exception("Could not parse Response");
  }

  private void parseHeaders() throws Exception {
    String line = input.readLine();
    while (!"".equals(line)) {
      Matcher match = headerPattern.matcher(line);
      if (match.find()) {
        String key = match.group(1);
        String value = match.group(2);
        headers.put(key, value);
      }
      line = input.readLine();
    }
  }

  private void parseBody() throws Exception {
    String lengthHeader = "Content-Length";
    if (hasHeader(lengthHeader)) {
      int bytesToRead = Integer.parseInt(getHeader(lengthHeader));
      body = input.read(bytesToRead);
    }
  }

  private void parseChunks() throws Exception {
    StringBuffer bodyBuffer = new StringBuffer();
    int chunkSize = readChunkSize();
    while (chunkSize != 0) {
      bodyBuffer.append(input.read(chunkSize));
      readCRLF();
      chunkSize = readChunkSize();
    }
    body = bodyBuffer.toString();

  }

  private int readChunkSize() throws Exception {
    String sizeLine = input.readLine();
    return Integer.parseInt(sizeLine, 16);
  }

  private void readCRLF() throws Exception {
    input.read(2);
  }

  public int getStatus() {
    return status;
  }

  public String getBody() {
    return body;
  }

  public String getHeader(String key) {
    return (String) headers.get(key);
  }

  public boolean hasHeader(String key) {
    return headers.containsKey(key);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Status: ").append(status).append("\n");
    buffer.append("Headers: ").append("\n");
    for (Iterator<String> iterator = headers.keySet().iterator(); iterator.hasNext();) {
      String key = (String) iterator.next();
      buffer.append("\t").append(key).append(": ").append(headers.get(key)).append("\n");

    }
    buffer.append("Body: ").append("\n");
    buffer.append(body);
    return buffer.toString();
  }

  public static ResponseParser performHttpRequest(String hostname, int hostPort, RequestBuilder builder) throws Exception {
    Socket socket = new Socket(hostname, hostPort);
    OutputStream socketOut = socket.getOutputStream();
    InputStream socketIn = socket.getInputStream();
    builder.send(socketOut);
    socketOut.flush();
    ResponseParser parser = new ResponseParser(socketIn);
    socket.close();
    return parser;
  }
}
