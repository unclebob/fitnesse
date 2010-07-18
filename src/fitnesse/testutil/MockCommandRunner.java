// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.components.CommandRunner;

public class MockCommandRunner extends CommandRunner {
  public MockCommandRunner() {
    super("", "");
  }

  public MockCommandRunner(String command, int exitCode) {
    super(command, "");
    this.exitCode = exitCode;
  }

  public void run() throws Exception {
  }

  public void join() throws Exception {
  }

  public void kill() throws Exception {
  }

  public void asynchronousStart() throws Exception {
  }

  public void setOutput(String output) {
    outputBuffer = new StringBuffer(output);
  }

  public void setError(String error) {
    errorBuffer = new StringBuffer(error);
  }

  public void addException(Exception e) {
    exceptions.add(e);
  }

  public void setExitCode(int i) {
    exitCode = i;
  }
  
  public long getExecutionTime() {
    return -1;
  }
}
