// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

public class EmployeePayRecord {
  public int id;
  private double salary;

  public EmployeePayRecord(int id, double salary) {
    this.id = id;
    this.salary = salary;
  }

  public double pay() {
    return salary;
  }
}
