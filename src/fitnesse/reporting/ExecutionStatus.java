// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.reporting;

public enum ExecutionStatus {
  OK("ok"),
  ERROR("error");

  private final String style;

  ExecutionStatus(String style) {
    this.style = style;
  }

  public String getStyle() {
    return style;
  }

  @Override
  public String toString() {
    return "Execution Status: " + style;
  }

}
