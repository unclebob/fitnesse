// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

public class ExecutionStatus {
  public static final ExecutionStatus OK = new ExecutionStatus("Tests Executed OK", "ok");
  public static final ExecutionStatus ERROR = new ExecutionStatus("Errors Occurred", "error");

  private String message;
  private String style;

  public ExecutionStatus(String message, String style) {
    this.message = message;
    this.style = style;
  }

  public String getMessage() {
    return message;
  }

  public String getStyle() {
    return style;
  }

  public String toString() {
    return "Execution Report: " + message;

  }

}
