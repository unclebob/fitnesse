// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.components;

import util.TimeMeasurement;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRunner {
  protected Process process;
  protected String input = "";
  protected List<Throwable> exceptions = new ArrayList<Throwable>();
  protected OutputStream stdin;
  protected InputStream stdout;
  protected InputStream stderr;
  protected StringBuffer outputBuffer = new StringBuffer();
  protected StringBuffer errorBuffer = new StringBuffer();
  protected int exitCode = -1;
  private TimeMeasurement timeMeasurement = new TimeMeasurement();
  private String command = "";
  private Map<String, String> environmentVariables;

  public CommandRunner() {
  }

  public CommandRunner(String command, String input) {
    this(command, input, null);
  }

  public CommandRunner(String command, String input, Map<String, String> environmentVariables) {
    this.command = command;
    this.input = input;
    this.environmentVariables = environmentVariables;
  }

  public void asynchronousStart() throws Exception {
    Runtime rt = Runtime.getRuntime();
    timeMeasurement.start();
    String[] environmentVariables = determineEnvironment();
    process = rt.exec(command, environmentVariables);
    stdin = process.getOutputStream();
    stdout = process.getInputStream();
    stderr = process.getErrorStream();

    new Thread(new OuputReadingRunnable(stdout, outputBuffer), "CommandRunner stdout").start();
    new Thread(new OuputReadingRunnable(stderr, errorBuffer), "CommandRunner error").start();

    sendInput();
  }

  private String[] determineEnvironment() {
    if (environmentVariables == null) {
      return null;
    }
    Map<String, String> systemVariables = new HashMap<String, String>(System.getenv());
    systemVariables.putAll(environmentVariables);
    List<String> systemVariableAssignments = new ArrayList<String>();
    for (Map.Entry<String, String> entry : systemVariables.entrySet()) {
      systemVariableAssignments.add(entry.getKey() + "=" + entry.getValue());
    }
    return systemVariableAssignments.toArray(new String[systemVariableAssignments.size()]);
  }

  public void run() throws Exception {
    asynchronousStart();
    join();
  }

  public void join() throws Exception {
    waitForDeathOf(process);
    timeMeasurement.stop();
    exitCode = process.exitValue();
  }

  private void waitForDeathOf(Process process) throws Exception {
    int timeStep = 100;
    for (int maxDelay = 2000; maxDelay > 0; maxDelay -= timeStep) {
      if (isDead(process)) {
        return;
      }
      Thread.sleep(timeStep);
    }
    System.err.println("Could not detect death of command line test runner.");
  }

  private boolean isDead(Process process) throws InterruptedException {
    try {
      process.exitValue();
      return true;
    } catch (IllegalThreadStateException e) {
      return false;
    }
  }

  public void kill() throws Exception {
    if (process != null) {
      process.destroy();
      join();
    }
  }

  protected void setCommand(String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }

  public String getOutput() {
    return outputBuffer.toString();
  }

  public String getError() {
    return errorBuffer.toString();
  }

  public List<Throwable> getExceptions() {
    return exceptions;
  }

  public boolean hasExceptions() {
    return exceptions.size() > 0;
  }

  public boolean wroteToErrorStream() {
    return errorBuffer.length() > 0;
  }

  public boolean wroteToOutputStream() {
    return outputBuffer.length() > 0;
  }

  public int getExitCode() {
    return exitCode;
  }

  public void exceptionOccurred(Exception e) {
    exceptions.add(e);
  }

  public long getExecutionTime() {
    return timeMeasurement.elapsed();
  }

  protected void sendInput() throws Exception {
    Thread thread = new Thread() {
      public void run() {
        try {
          stdin.write(input.getBytes("UTF-8"));
          stdin.flush();
        } catch (Exception e) {
          exceptionOccurred(e);
        } finally {
          try {
            stdin.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    };
    thread.start();
    thread.join();

  }

  private void readOutput(InputStream input, StringBuffer buffer) {
    try {
      int c;
      while ((c = input.read()) != -1)
        buffer.append((char) c);
    } catch (Exception e) {
      exceptionOccurred(e);
    }
  }

  private class OuputReadingRunnable implements Runnable {
    public InputStream input;
    public StringBuffer buffer;

    public OuputReadingRunnable(InputStream input, StringBuffer buffer) {
      this.input = input;
      this.buffer = buffer;
    }

    public void run() {
      readOutput(input, buffer);
    }
  }
}