// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import util.StreamReader;
import fitnesse.components.Base64;

public class RequestBuilder {
  private static final byte[] ENDL = "\r\n".getBytes();

  private String resource;
  private String method = "GET";
  private List<InputStream> bodyParts = new LinkedList<InputStream>();
  private HashMap<String, String> headers = new HashMap<String, String>();
  private HashMap<String, Object> inputs = new HashMap<String, Object>();
  private String host;
  private int port;
  private String boundary;
  private boolean isMultipart = false;
  private int bodyLength = 0;

  public RequestBuilder(String resource) {
    this.resource = resource;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void addHeader(String key, String value) {
    headers.put(key, value);
  }

  public String getText() throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    send(output);
    return output.toString();
  }

  private String buildRequestLine() throws Exception {
    StringBuffer text = new StringBuffer();
    text.append(method).append(" ").append(resource);
    if (isGet()) {
      String inputString = inputString();
      if (inputString.length() > 0)
        text.append("?").append(inputString);
    }
    text.append(" HTTP/1.1");
    return text.toString();
  }

  private boolean isGet() {
    return method.equals("GET");
  }

  public void send(OutputStream output) throws Exception {
    output.write(buildRequestLine().getBytes("UTF-8"));
    output.write(ENDL);
    buildBody();
    sendHeaders(output);
    output.write(ENDL);
    sendBody(output);
  }

  private void sendHeaders(OutputStream output) throws Exception {
    addHostHeader();
    for (Iterator<String> iterator = headers.keySet().iterator(); iterator.hasNext();) {
      String key = iterator.next();
      output.write((key + ": " + headers.get(key)).getBytes("UTF-8"));
      output.write(ENDL);
    }
  }

  private void buildBody() throws Exception {
    if (!isMultipart) {
      byte[] bytes = inputString().getBytes("UTF-8");
      bodyParts.add(new ByteArrayInputStream(bytes));
      bodyLength += bytes.length;
    } else {
      for (Iterator<String> iterator = inputs.keySet().iterator(); iterator.hasNext();) {
        String name = iterator.next();
        Object value = inputs.get(name);
        StringBuffer partBuffer = new StringBuffer();
        partBuffer.append("--").append(getBoundary()).append("\r\n");
        partBuffer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append("\r\n");
        if (value instanceof InputStreamPart) {
          InputStreamPart part = (InputStreamPart) value;
          partBuffer.append("Content-Type: ").append(part.contentType).append("\r\n");
          partBuffer.append("\r\n");
          addBodyPart(partBuffer.toString());
          bodyParts.add(part.input);
          bodyLength += part.size;
          addBodyPart("\r\n");
        } else {
          partBuffer.append("Content-Type: text/plain").append("\r\n");
          partBuffer.append("\r\n");
          partBuffer.append(value);
          partBuffer.append("\r\n");
          addBodyPart(partBuffer.toString());
        }
      }
      StringBuffer tail = new StringBuffer();
      tail.append("--").append(getBoundary()).append("--").append("\r\n");
      addBodyPart(tail.toString());
    }
    addHeader("Content-Length", bodyLength + "");
  }

  private void addBodyPart(String input) throws Exception {
    byte[] bytes = input.toString().getBytes("UTF-8");
    bodyParts.add(new ByteArrayInputStream(bytes));
    bodyLength += bytes.length;
  }

  private void sendBody(OutputStream output) throws Exception {
    for (Iterator<InputStream> iterator = bodyParts.iterator(); iterator.hasNext();) {
      InputStream input = iterator.next();

      StreamReader reader = new StreamReader(input);
      while (!reader.isEof()) {
        byte[] bytes = reader.readBytes(1000);
        output.write(bytes);
      }
    }
  }

  private void addHostHeader() {
    if (host != null)
      addHeader("Host", host + ":" + port);
    else
      addHeader("Host", "");
  }

  public void addInput(String key, Object value) throws Exception {
    inputs.put(key, value);
  }

  public String inputString() throws Exception {
    StringBuffer buffer = new StringBuffer();
    boolean first = true;
    for (Iterator<String> iterator = inputs.keySet().iterator(); iterator.hasNext();) {
      String key = iterator.next();
      String value = (String) inputs.get(key);
      if (!first)
        buffer.append("&");
      buffer.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
      first = false;
    }
    return buffer.toString();
  }

  public void addCredentials(String username, String password) throws Exception {
    String rawUserpass = username + ":" + password;
    String userpass = Base64.encode(rawUserpass);
    addHeader("Authorization", "Basic " + userpass);
  }

  public void setHostAndPort(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getBoundary() {
    if (boundary == null)
      boundary = "----------" + new Random().nextInt() + "BoUnDaRy";
    return boundary;
  }

  public void addInputAsPart(String name, Object content) throws Exception {
    multipart();
    addInput(name, content);
  }

  public void addInputAsPart(String name, InputStream input, int size, String contentType) throws Exception {
    addInputAsPart(name, new InputStreamPart(input, size, contentType));
  }

  private void multipart() {
    if (!isMultipart) {
      isMultipart = true;
      setMethod("POST");
      addHeader("Content-Type", "multipart/form-data; boundary=" + getBoundary());
    }
  }

  private static class InputStreamPart {
    public InputStream input;
    public int size;
    public String contentType;

    public InputStreamPart(InputStream input, int size, String contentType) {
      this.input = input;
      this.size = size;
      this.contentType = contentType;
    }
  }
}
