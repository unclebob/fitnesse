// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.employeePayroll;

import fitnesse.fixtures.RowEntryFixture;
import fit.Parse;

public class Employees extends RowEntryFixture
{
  public int id;
  public String name;
  public String address;
  public double salary;

  public void enterRow() throws Exception
  {

  }
}
