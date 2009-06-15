// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

public interface TestSystemListener {
  public void acceptOutputFirst(String output) throws Exception;

  public void testComplete(TestSummary testSummary) throws Exception;

  public void exceptionOccurred(Throwable e);
}
