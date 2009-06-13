// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.StreamReader;
import fitnesse.components.Base64;

public class Request {
  private static final Pattern requestLinePattern = Pattern
  .compile("(\\p{Upper}+?) ([^\\s]+)");
  private static final Pattern requestUriPattern = Pattern
  .compile("([^?]+)\\??(.*)");
  private static final Pattern queryStringPattern = Pattern
  .compile("([^=&]*)=?([^&]*)&?");
  private static final Pattern headerPattern = Pattern.compile("([^:]*): (.*)");
  private static final Pattern boundaryPattern = Pattern
  .compile("boundary=(.*)");
  private static final Pattern multipartHeaderPattern = Pattern
  .compile("([^ =]+)=\\\"([^\"]*)\\\"");

  private static Collection<String> allowedMethods = buildAllowedMethodList();

  protected StreamReader input;
  protected String requestURI;
  private String resource;
  protected String queryString;
  protected Map<String, Object> inputs = new HashMap<String, Object>();
  protected Map<String, Object> headers = new HashMap<String, Object>();
  protected String entityBody = "";
  protected String requestLine;
  protected String authorizationUsername;
  protected String authorizationPassword;
  private boolean hasBeenParsed;
  private long bytesParsed = 0;

  public static Set<String> buildAllowedMethodList() {
    Set<String> methods = new HashSet<String>(20);
    methods.add("GET");
    methods.add("POST");
    return methods;
  }

  protected Request() {
  }

  public Request(InputStream input) throws Exception {
    this.input = new StreamReader(new BufferedInputStream(input));
  }

  public void parse() throws Exception {
    readAndParseRequestLine();
    headers = parseHeaders(input);
    parseEntityBody();
    hasBeenParsed = true;
  }

  private void readAndParseRequestLine() throws Exception {
    requestLine = input.readLine();
    Matcher match = requestLinePattern.matcher(requestLine);
    checkRequestLine(match);
    requestURI = match.group(2);
    parseRequestUri(requestURI);
  }

  private Map<String, Object> parseHeaders(StreamReader reader)
  throws Exception {
    HashMap<String, Object> headers = new HashMap<String, Object>();
    String line = reader.readLine();
    while (!"".equals(line)) {
      Matcher match = headerPattern.matcher(line);
      if (match.find()) {
        String key = match.group(1);
        String value = match.group(2);
        headers.put(key.toLowerCase(), value);
      }
      line = reader.readLine();
    }
    return headers;
  }

  private void parseEntityBody() throws Exception {
    if (hasHeader("Content-Length")) {
      String contentType = (String) getHeader("Content-Type");
      if (contentType != null && contentType.startsWith("multipart/form-data")) {
        Matcher match = boundaryPattern.matcher(contentType);
        match.find();
        parseMultiPartContent(match.group(1));
      } else {
        entityBody = input.read(getContentLength());
        parseQueryString(entityBody);
      }
    }
  }

  public int getContentLength() {
    return Integer.parseInt((String) getHeader("Content-Length"));
  }

  private void parseMultiPartContent(String boundary) throws Exception {
    boundary = "--" + boundary;

    int numberOfBytesToRead = getContentLength();
    accumulateBytesReadAndReset();
    input.readUpTo(boundary);
    while (numberOfBytesToRead - input.numberOfBytesConsumed() > 10) {
      input.readLine();
      Map<String, Object> headers = parseHeaders(input);
      String contentDisposition = (String) headers.get("content-disposition");
      Matcher matcher = multipartHeaderPattern.matcher(contentDisposition);
      while (matcher.find())
        headers.put(matcher.group(1), matcher.group(2));

      String name = (String) headers.get("name");
      Object value;
      if (headers.containsKey("filename"))
        value = createUploadedFile(headers, input, boundary);
      else
        value = input.readUpTo("\r\n" + boundary);

      inputs.put(name, value);
    }
  }

  private void accumulateBytesReadAndReset() {
    bytesParsed += input.numberOfBytesConsumed();
    input.resetNumberOfBytesConsumed();
  }

  private Object createUploadedFile(Map<String, Object> headers,
      StreamReader reader, String boundary) throws Exception {
    String filename = (String) headers.get("filename");
    String contentType = (String) headers.get("content-type");
    File tempFile = File.createTempFile("FitNesse", ".uploadedFile");
    OutputStream output = null;
    try {
      output = new BufferedOutputStream(new FileOutputStream(tempFile));
      reader.copyBytesUpTo("\r\n" + boundary, output);
      return new UploadedFile(filename, contentType, tempFile);
    } finally {
      if (output != null)
        output.close();
    }
  }

  private void checkRequestLine(Matcher match) throws HttpException {
    if (!match.find())
      throw new HttpException(
      "The request string is malformed and can not be parsed");
    if (!allowedMethods.contains(match.group(1)))
      throw new HttpException("The " + match.group(1)
          + " method is not currently supported");
  }

  public void parseRequestUri(String requestUri) {
    Matcher match = requestUriPattern.matcher(requestUri);
    match.find();
    resource = stripLeadingSlash(match.group(1));
    queryString = match.group(2);
    parseQueryString(queryString);
  }

  protected void parseQueryString(String queryString) {
    Matcher match = queryStringPattern.matcher(queryString);
    while (match.find()) {
      String key = match.group(1);
      String value = decodeContent(match.group(2));
      addUniqueInputString(key, value);
    }
  }

  private void addUniqueInputString(String key, String value) {
    Object existingItem = inputs.put(key, value);
    if (itemExistAndMismatches(existingItem, value)) {
      inputs.put(key, concatenateItems((String)existingItem, value));
    }
  }

  private String concatenateItems(String existingItem, String value) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(existingItem);
    buffer.append(',');
    buffer.append(value);
    return buffer.toString();
  }

  private boolean itemExistAndMismatches(Object existingItem, String value) {
    return existingItem instanceof String && !value.equals(existingItem);
  }

  public String getRequestLine() {
    return requestLine;
  }

  public String getRequestUri() {
    return requestURI;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getQueryString() {
    return queryString;
  }

  public boolean hasInput(String key) {
    return inputs.containsKey(key);
  }

  public Object getInput(String key) {
    return inputs.get(key);
  }

  public boolean hasHeader(String key) {
    return headers.containsKey(key.toLowerCase());
  }

  public Object getHeader(String key) {
    return headers.get(key.toLowerCase());
  }

  public String getBody() {
    return entityBody;
  }

  private String stripLeadingSlash(String url) {
    return url.substring(1);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("--- Request Start ---\n");
    buffer.append("Request URI:  ").append(requestURI).append('\n');
    buffer.append("Resource:     ").append(resource).append('\n');
    buffer.append("Query String: ").append(queryString).append('\n');
    buffer.append("Hearders: (" + headers.size() + ")\n");
    addMap(headers, buffer);
    buffer.append("Form Inputs: (" + inputs.size() + ")\n");
    addMap(inputs, buffer);
    buffer.append("Entity Body: \n");
    buffer.append(entityBody).append('\n');
    buffer.append("--- End Request ---\n");

    return buffer.toString();
  }

  private void addMap(Map<String, Object> map, StringBuffer buffer) {
    if (map.isEmpty()) {
      buffer.append("\tempty");
    }
    for (Entry<String, Object> entry: map.entrySet()) {
      String value = entry.getValue() == null ? null : escape(entry.getValue().toString());
      buffer.append("\t" + escape(entry.getKey()) + " \t-->\t " + value + "\n");
    }
  }

  private String escape(String foo) {
    return foo.replaceAll("[\n\r]+", "|");
  }

  public static String decodeContent(String content) {
    try {
      return URLDecoder.decode(content, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return "URLDecoder Error";
    }
  }

  public boolean hasBeenParsed() {
    return hasBeenParsed;
  }

  public String getUserpass(String headerValue) throws Exception {
    String encodedUserpass = headerValue.substring(6);
    return Base64.decode(encodedUserpass);
  }

  public void getCredentials() throws Exception {
    if (authorizationUsername != null)
      return;
    if (hasHeader("Authorization")) {
      String authHeader = getHeader("Authorization").toString();
      String userpass = getUserpass(authHeader);
      String[] values = userpass.split(":");
      if (values.length == 2) {
        authorizationUsername = values[0];
        authorizationPassword = values[1];
      }
    }
  }

  public String getAuthorizationUsername() {
    return authorizationUsername;
  }

  public String getAuthorizationPassword() {
    return authorizationPassword;
  }

  public long numberOfBytesParsed() {
    return bytesParsed + input.numberOfBytesConsumed();
  }

  public void setCredentials(String username, String password) {
    authorizationUsername = username;
    authorizationPassword = password;
  }
}
