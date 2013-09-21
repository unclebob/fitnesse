// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

public class PayCheckRecord {
  public int employeeId;
  public String date;
  public String name;
  private double salary;

  public PayCheckRecord(int employeeId, String date, String name, double salary) {
    this.employeeId = employeeId;
    this.date = date;
    this.name = name;
    this.salary = salary;
  }

  public double pay() {
    return salary;
  }
}
