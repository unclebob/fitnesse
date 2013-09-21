// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package eg.employeePayroll;

import fit.RowFixture;

public class PaycheckInspector extends RowFixture {
  public Object[] query() throws Exception  // get rows to be compared
  {
    return new Object[0];
  }

  public Class<?> getTargetClass()             // get expected type of row
  {
    return PayCheck.class;
  }
}
