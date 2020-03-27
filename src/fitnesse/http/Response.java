// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import util.FileUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

public abstract class Response {
  public enum Format {
    XML("text/xml"),
    HTML("text/html; charset=" + FileUtil.CHARENCODING),
    TEXT("text/text"),
    TSV("text/tab-separated-values"),
    JSON("application/json"),
    JUNIT("text/junit");

    private final String contentType;

    Format(String contentType) {
      this.contentType = contentType;
    }

    public String getContentType() {
      return contentType;
    }

  }

  protected static final String CRLF = "\r\n";

  public static SimpleDateFormat makeStandardHttpDateFormat() {
    // SimpleDateFormat is not thread safe, so we need to create each
    // instance independently.
    SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    df.setTimeZone(TimeZone.getTimeZone("GMT"));
    return df;
  }

  private int status = 200;
  private HashMap<String, String> headers = new HashMap<>(17);
  private String contentType = Format.HTML.contentType;
  private boolean withHttpHeaders = true;

  public Response(String formatString) {
    Format format;

    if ("html".equalsIgnoreCase(formatString)) {
      format = Format.HTML;
    } else if ("xml".equalsIgnoreCase(formatString)) {
      format = Format.XML;
    } else if ("junit".equalsIgnoreCase(formatString)) {
      format = Format.JUNIT;
    } else if ("text".equalsIgnoreCase(formatString)) {
      format = Format.TEXT;
    } else if ("tsv".equalsIgnoreCase(formatString)) {
      format = Format.TSV;
    } else {
      format = Format.HTML;
    }
    setContentType(format.getContentType());
  }

  public Response(String format, int status) {
    this(format);
    this.status = status;
  }

  public boolean isXmlFormat() {
    return Format.XML.contentType.equals(contentType);
  }

  public boolean isHtmlFormat() {
    return Format.HTML.contentType.equals(contentType);
  }

  public boolean isTextFormat() {
    return Format.TEXT.contentType.equals(contentType);
  }

  public boolean isTabSeparatedFormat() {
    return Format.TSV.contentType.equals(contentType);
  }

  public boolean isJunitFormat() {
	    return Format.JUNIT.contentType.equals(contentType);
  }

  public boolean hasContent() {
    return contentType != null;
  }

  public abstract void sendTo(ResponseSender sender) throws IOException;

  public abstract int getContentSize();

  public int getStatus() {
    return status;
  }

  public void setStatus(int s) {
    status = s;
  }

  public void withoutHttpHeaders() {
    this.withHttpHeaders = false;
  }

  public final String makeHttpHeaders() {
    if (!withHttpHeaders)
      return "";
    if (hasContent()) {
      addContentHeaders();
    }
    StringBuilder text = new StringBuilder();
    if (!Format.TEXT.contentType.equals(contentType)) {
      text.append("HTTP/1.1 ").append(status).append(" ").append(
        getReasonPhrase()).append(CRLF);

      for (Entry<String, String> entry: headers.entrySet()) {
        appendHeader(text, entry.getKey(), entry.getValue());
      }

      text.append(CRLF);
    }
    return text.toString();
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String type) {
    contentType = type;
  }

  private void noContent() {
    contentType = null;
  }

  public void setContentType(Format format) {
    contentType = format.getContentType();
  }

  public void redirect(String contextRoot, String location) {
    status = 303;
    addHeader("Location", contextRoot + location);
  }

  public void notModified(Date lastModified, Date date) {
    status = 304;
    noContent();
    SimpleDateFormat httpDateFormat = makeStandardHttpDateFormat();
    addHeader("Date", httpDateFormat.format(date));
    setLastModifiedHeader(lastModified);
    addHeader("Cache-Control", "private");
  }

  public void setMaxAge(int age) {
    addHeader("Cache-Control", "max-age=" + age);
  }

  public void setLastModifiedHeader(Date date) {
    addHeader("Last-Modified", makeStandardHttpDateFormat().format(date));
  }

  public void addHeader(String key, String value) {
    headers.put(key, value);
  }

  public String getHeader(String key) {
    return headers.get(key);
  }

  public byte[] getEncodedBytes(String value) throws UnsupportedEncodingException {
    return value.getBytes(FileUtil.CHARENCODING);
  }

  protected StringBuilder appendHeader(StringBuilder builder, String header, String value) {
    builder.append(header).append(": ").append(value).append(CRLF);
    return builder;
  }

  protected void addContentHeaders() {
    addHeader("Content-Type", getContentType());
  }

  protected String getReasonPhrase() {
    return getReasonPhrase(status);
  }

  private static final Map<Integer, String> reasonCodes = new HashMap<Integer, String>() {
    private static final long serialVersionUID = 1L;

    {
      put(100, "Continue");
      put(101, "Switching Protocols");
      put(200, "OK");
      put(201, "Created");
      put(202, "Accepted");
      put(203, "Non-Authoritative Information");
      put(204, "No Content");
      put(205, "Reset Content");
      put(300, "Multiple Choices");
      put(301, "Moved Permanently");
      put(302, "Found");
      put(303, "See Other");
      put(304, "Not Modified");
      put(305, "Use Proxy");
      put(307, "Temporary Redirect");
      put(400, "Bad Request");
      put(401, "Unauthorized");
      put(402, "Payment Required");
      put(403, "Forbidden");
      put(404, "Not Found");
      put(405, "Method Not Allowed");
      put(406, "Not Acceptable");
      put(407, "Proxy Authentication Required");
      put(408, "Request Time-out");
      put(409, "Conflict");
      put(410, "Gone");
      put(411, "Length Required");
      put(412, "Precondition Failed");
      put(413, "Request Entity Too Large");
      put(414, "Request-URI Too Large");
      put(415, "Unsupported Media Type");
      put(416, "Requested range not satisfiable");
      put(417, "SlimExpectation Failed");
      put(500, "Internal Server Error");
      put(501, "Not Implemented");
      put(502, "Bad Gateway");
      put(503, "Service Unavailable");
      put(504, "Gateway Time-out");
      put(505, "HTTP Version not supported");
    }
  };

  public static String getReasonPhrase(int status) {
    String reasonPhrase = reasonCodes.get(status);
    return reasonPhrase == null ? "Unknown Status" : reasonPhrase;
  }
}
