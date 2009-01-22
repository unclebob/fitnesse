// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.util.GregorianCalendar;

public class LogData implements Cloneable {
  public String host;
  public GregorianCalendar time;
  public String requestLine;
  public int status;
  public int size;
  public String username;

  public Object clone() throws CloneNotSupportedException {
    LogData newData = (LogData) super.clone();
    newData.time = (GregorianCalendar) time.clone();

    return newData;
  }
}
