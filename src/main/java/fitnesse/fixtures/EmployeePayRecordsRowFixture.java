// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.RowFixture;

public class EmployeePayRecordsRowFixture extends RowFixture {
  public Object[] query() throws Exception {
    EmployeePayRecord[] records = new EmployeePayRecord[2];
    records[0] = new EmployeePayRecord(1, 1000);
    records[1] = new EmployeePayRecord(2, 2000);
    return records;
  }

  public Class<?> getTargetClass() {
    return EmployeePayRecord.class;
  }
}
