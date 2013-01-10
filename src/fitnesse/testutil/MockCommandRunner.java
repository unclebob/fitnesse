// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import java.io.IOException;

import fitnesse.components.CommandRunner;

public class MockCommandRunner extends CommandRunner {
  public MockCommandRunner() {
    super("", "");
  }

  public MockCommandRunner(String command, int exitCode) {
    super(command, "", exitCode);
  }

  public void run() {
  }

  public void join() {
  }

  public void kill() {
  }

  public void asynchronousStart() throws IOException {
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
