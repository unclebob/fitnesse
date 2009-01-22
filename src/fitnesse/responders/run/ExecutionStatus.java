// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

public class ExecutionStatus {
  public static final ExecutionStatus OK = new ExecutionStatus("Tests Executed OK", "ok.gif");
  public static final ExecutionStatus OUTPUT = new ExecutionStatus("Output Captured", "output.gif");
  public static final ExecutionStatus ERROR = new ExecutionStatus("Errors Occurred", "error.gif");

  private String message;
  private String iconFilename;

  public ExecutionStatus(String message, String iconFilename) {
    this.message = message;
    this.iconFilename = iconFilename;
  }

  public String getMessage() {
    return message;
  }

  public String getIconFilename() {
    return iconFilename;
  }

  public String toString() {
    return "Execution Report: " + message;

  }

}
