// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import fitnesse.responders.run.TestSummary;

public class MockResultFormatter implements ResultFormatter {
  public List<PageResult> results = new LinkedList<PageResult>();
  public TestSummary finalSummary;
  public StringBuffer output = new StringBuffer("Mock Results:\n");

  public void acceptResult(PageResult result) throws Exception {
    results.add(result);
    output.append(result.toString());
  }

  public void acceptFinalCount(TestSummary testSummary) throws Exception {
    finalSummary = testSummary;
    output.append("Finals Counts: " + testSummary.toString());
  }

  public int getByteCount() {
    return output.toString().getBytes().length;
  }

  public InputStream getResultStream() throws Exception {
    return new ByteArrayInputStream(output.toString().getBytes());
  }

}
