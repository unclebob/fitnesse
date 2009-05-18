// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fit.Counts;
import fit.FitProtocol;
import fitnesse.components.ContentBuffer;
import fitnesse.responders.run.TestSummary;

public class CachingResultFormatter implements ResultFormatter {
  private ContentBuffer buffer;
  public List<ResultHandler> subHandlers = new LinkedList<ResultHandler>();

  public CachingResultFormatter() throws Exception {
    buffer = new ContentBuffer(".results");
  }

  public void acceptResult(PageResult result) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    FitProtocol.writeData(result.toString() + "\n", output);
    buffer.append(output.toByteArray());

    for (Iterator<ResultHandler> iterator = subHandlers.iterator(); iterator.hasNext();)
      iterator.next().acceptResult(result);
  }

  public void acceptFinalCount(TestSummary testSummary) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Counts counts = new Counts(testSummary.getRight(), testSummary.getWrong(), testSummary.getIgnores(), testSummary.getExceptions());
    FitProtocol.writeCounts(counts, output);
    buffer.append(output.toByteArray());

    for (Iterator<ResultHandler> iterator = subHandlers.iterator(); iterator.hasNext();)
      iterator.next().acceptFinalCount(testSummary);
  }

  public int getByteCount() throws Exception {
    return buffer.getSize();
  }

  public InputStream getResultStream() throws Exception {
    return buffer.getNonDeleteingInputStream();
  }

  public void cleanUp() throws Exception {
    buffer.delete();
  }

  public void addHandler(ResultHandler handler) {
    subHandlers.add(handler);
  }
}
