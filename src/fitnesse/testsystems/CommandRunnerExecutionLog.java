// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import util.StringUtil;

public class CommandRunnerExecutionLog implements ExecutionLog {

  private final CommandRunner runner;
  private final List<Throwable> exceptions = new LinkedList<Throwable>();

  public CommandRunnerExecutionLog(CommandRunner client) {
    runner = client;
  }

  @Override
  public void addException(Throwable e) {
    exceptions.add(e);
  }

  @Override
  public List<Throwable> getExceptions() {
    List<Throwable> exc = new ArrayList<Throwable>(exceptions);
    exc.addAll(runner.getExceptions());
    return exc;
  }

  @Override
  public boolean hasCapturedOutput() {
    return runner.wroteToErrorStream() || runner.wroteToOutputStream();
  }

  @Override
  public String getCommand() {
    return StringUtil.join(Arrays.asList(runner.getCommand()), " ");
  }

  @Override
  public long getExecutionTime() {
    return runner.getExecutionTime();
  }

  @Override
  public int getExitCode() {
    return runner.getExitCode();
  }

  @Override
  public String getCapturedOutput() {
    return runner.getOutput();
  }

  @Override
  public String getCapturedError() {
    return runner.getError();
  }

}
