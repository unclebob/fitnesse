// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import util.StreamReader;
import fitnesse.util.Base64;

import static util.FileUtil.CHARENCODING;

public class RequestBuilder {
  private static final byte[] ENDL = "\r\n".getBytes();
  private static final Random RANDOM_GENERATOR = new SecureRandom();

  private String resource;
  private String method = "GET";
  private List<InputStream> bodyParts = new LinkedList<>();
  private HashMap<String, String> headers = new HashMap<>();
  private HashMap<String, Object> inputs = new HashMap<>();
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

  private String buildRequestLine() throws UnsupportedEncodingException {
    StringBuilder text = new StringBuilder();
    text.append(method).append(" ").append(resource);
    if (isGet()) {
      String inputString = inputString();
      if (!inputString.isEmpty())
        text.append("?").append(inputString);
    }
    text.append(" HTTP/1.1");
    return text.toString();
  }

  private boolean isGet() {
    return method.equals("GET");
  }

  public void send(OutputStream output) throws IOException {
    output.write(buildRequestLine().getBytes(CHARENCODING));
    output.write(ENDL);
    buildBody();
    sendHeaders(output);
    output.write(ENDL);
    sendBody(output);
  }

  private void sendHeaders(OutputStream output) throws IOException {
    addHostHeader();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      output.write((entry.getKey() + ": " + entry.getValue()).getBytes(CHARENCODING));
      output.write(ENDL);
    }
  }

  private void buildBody() throws IOException {
    if (!isMultipart) {
      byte[] bytes = inputString().getBytes(CHARENCODING);
      bodyParts.add(new ByteArrayInputStream(bytes));
      bodyLength += bytes.length;
    } else {
      for (Map.Entry<String, Object> entry : inputs.entrySet()) {
        String name = entry.getKey();
        Object value = entry.getValue();
        StringBuilder partBuffer = new StringBuilder();
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
      String tail = "--" + getBoundary() + "--" + "\r\n";
      addBodyPart(tail);
    }
    addHeader("Content-Length", bodyLength + "");
  }

  private void addBodyPart(String input) throws UnsupportedEncodingException {
    byte[] bytes = input.getBytes(CHARENCODING);
    bodyParts.add(new ByteArrayInputStream(bytes));
    bodyLength += bytes.length;
  }

  private void sendBody(OutputStream output) throws IOException {
    for (InputStream input : bodyParts) {
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

  public void addInput(String key, Object value) {
    inputs.put(key, value);
  }

  public String inputString() throws UnsupportedEncodingException {
    StringBuilder buffer = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, Object> entry : inputs.entrySet()) {
      String value = (String) entry.getValue();
      if (!first)
        buffer.append("&");
      String key = entry.getKey();
      buffer.append(key).append("=").append(URLEncoder.encode(value, CHARENCODING));
      first = false;
    }
    return buffer.toString();
  }

  public void addCredentials(String username, String password) {
    String rawUserpass = username + ":" + password;
    String userpass = Base64.encode(rawUserpass);
    addHeader("Authorization", "Basic " + userpass);
  }

  public void setHostAndPort(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getBoundary() {
    if (boundary == null) {
      boundary = "----------" + RANDOM_GENERATOR.nextInt() + "BoUnDaRy";
    }
    return boundary;
  }

  public void addInputAsPart(String name, Object content) {
    multipart();
    addInput(name, content);
  }

  public void addInputAsPart(String name, InputStream input, int size, String contentType) {
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
