// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.test;

public class ExecuteThrowsReportableException {
  public void setX(int x){

  }
  public void execute() {
    throw new RuntimeException("A Reportable Exception");
  }
}
