// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.util.Calendar;

public class LogData {
  public final String host;
  public final Calendar time;
  public final String requestLine;
  public final int status;
  public final int size;
  public final String username;

  public LogData(String host, Calendar time, String requestLine, int status, int size, String username) {
    this.host = host;
    this.time = time;
    this.requestLine = requestLine;
    this.status = status;
    this.size = size;
    this.username = username;
  }
}
