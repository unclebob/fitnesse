// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.RowFixture;

public class PayCheckRecordFixture extends RowFixture {
  public Object[] query() throws Exception {
    PayCheckRecord[] payCheckRecords = new PayCheckRecord[4];
    payCheckRecords[0] = new PayCheckRecord(1, "3/1/03", "Bob", 1000);
    payCheckRecords[1] = new PayCheckRecord(2, "3/1/03", "Bill", 2002);
    payCheckRecords[2] = new PayCheckRecord(1, "4/1/03", "Bob", 1015);
    payCheckRecords[3] = new PayCheckRecord(2, "4/1/03", "Bill", 2003);
    return payCheckRecords;
  }

  public Class<?> getTargetClass() {
    return PayCheckRecord.class;
  }
}
