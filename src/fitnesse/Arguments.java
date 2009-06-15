// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

public class Arguments {
  public static final String DEFAULT_PATH = ".";
  public static final String DEFAULT_ROOT = "FitNesseRoot";
  public static final int DEFAULT_PORT = 80;
  public static final int DEFAULT_VERSION_DAYS = 14;

  private String rootPath = DEFAULT_PATH;
  private int port = DEFAULT_PORT;
  private String rootDirectory = DEFAULT_ROOT;
  private String logDirectory;
  private boolean omitUpdate = false;
  private int daysTillVersionsExpire = DEFAULT_VERSION_DAYS;
  private String userpass;

  public String getRootPath() {
    return rootPath;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public int getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = Integer.parseInt(port);
  }

  public String getRootDirectory() {
    return rootDirectory;
  }

  public void setRootDirectory(String rootDirectory) {
    this.rootDirectory = rootDirectory;
  }

  public String getLogDirectory() {
    return logDirectory;
  }

  public void setLogDirectory(String logDirectory) {
    this.logDirectory = logDirectory;
  }

  public void setOmitUpdates(boolean omitUpdates) {
    this.omitUpdate = omitUpdates;
  }

  public boolean isOmittingUpdates() {
    return omitUpdate;
  }

  public void setUserpass(String userpass) {
    this.userpass = userpass;
  }

  public String getUserpass() {
    if (userpass == null || userpass.length() == 0)
      return null;
    else
      return userpass;
  }

  public int getDaysTillVersionsExpire() {
    return daysTillVersionsExpire;
  }

  public void setDaysTillVersionsExpire(String daysTillVersionsExpire) {
    this.daysTillVersionsExpire = Integer.parseInt(daysTillVersionsExpire);
  }
}
