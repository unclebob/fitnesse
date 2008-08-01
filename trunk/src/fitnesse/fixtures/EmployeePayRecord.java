// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

public class EmployeePayRecord
{
	public int id;
	private double salary;

	public EmployeePayRecord(int id, double salary)
	{
		this.id = id;
		this.salary = salary;
	}

	public double pay()
	{
		return salary;
	}
}
