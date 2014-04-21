// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.testsystems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.TimeMeasurement;

public class CommandRunner {
  private static final String DEFAULT_CHARSET_NAME = "UTF-8";
  private static final Logger LOG = Logger.getLogger(CommandRunner.class.getName());

  private Process process;
  private String input = "";
  protected List<Throwable> exceptions = new ArrayList<Throwable>();
  protected StringBuffer outputBuffer = new StringBuffer();
  protected StringBuffer errorBuffer = new StringBuffer();
  protected int exitCode = -1;
  private TimeMeasurement timeMeasurement = new TimeMeasurement();
  private String[] command;
  private Map<String, String> environmentVariables;
  private int timeout;

  /**
   *
   * @param command
   * @param input
   * @param environmentVariables
   * @param timeout Time-out in seconds.
   */
  public CommandRunner(String[] command, String input, Map<String, String> environmentVariables, int timeout) {
    this.command = command;
    this.input = input;
    this.environmentVariables = environmentVariables;
    this.timeout = timeout;
  }

  public CommandRunner(String[] command, String input, Map<String, String> environmentVariables) {
    this(command, input, environmentVariables, 2);
  }

  protected CommandRunner(String[] command, String input, int exitCode) {
    this(command, input, null);
    this.exitCode = exitCode;
  }

  public void asynchronousStart() throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.environment().putAll(determineEnvironment());
    timeMeasurement.start();
    process = processBuilder.start();

    OutputStream stdin = process.getOutputStream();
    InputStream stdout = process.getInputStream();
    InputStream stderr = process.getErrorStream();

    new Thread(new OutputReadingRunnable(stdout, outputBuffer), "CommandRunner stdout").start();
    new Thread(new OutputReadingRunnable(stderr, errorBuffer), "CommandRunner error").start();

    sendInput(stdin);
  }

  private Map<String, String> determineEnvironment() {
    if (environmentVariables == null) {
      return Collections.emptyMap();
    }
    Map<String, String> systemVariables = new HashMap<String, String>(System.getenv());
    systemVariables.putAll(environmentVariables);
    return systemVariables;
  }

  public void run() throws IOException {
    asynchronousStart();
    join();
  }

  public void join() {
    waitForDeathOf(process);
    timeMeasurement.stop();
    if (isDead(process)) {
      exitCode = process.exitValue();
    }
  }

  private void waitForDeathOf(Process process) {
    int timeStep = 100;
    int maxDelay = timeout * 1000;
    try {
      for (int delayed = 0; delayed < maxDelay; delayed += timeStep) {
        if (isDead(process)) {
          return;
        }
        Thread.sleep(timeStep);
      }
    } catch (InterruptedException e) {
      LOG.log(Level.FINE, "Wait for death of process " + process + " interrupted", e);
    }
    LOG.warning("Could not detect death of command line test runner.");
  }

  private boolean isDead(Process process) {
    try {
      process.exitValue();
      return true;
    } catch (IllegalThreadStateException e) {
      return false;
    }
  }

  public void kill() {
    if (process != null) {
      process.destroy();
      join();
    }
  }

  protected void setCommand(String[] command) {
    this.command = command;
  }

  public String[] getCommand() {
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

  // Used to catch exceptions thrown from the read and write threads.
  public void exceptionOccurred(Exception e) {
    exceptions.add(e);
  }

  public long getExecutionTime() {
    return timeMeasurement.elapsed();
  }

  protected void sendInput(OutputStream stdin) throws IOException {
    try {
      stdin.write(input.getBytes(DEFAULT_CHARSET_NAME));
      stdin.flush();
    } finally {
      try {
        stdin.close();
      } catch (IOException e) {
        LOG.log(Level.FINE, "Failed to close output stream", e);
      }
    }
  }

  private class OutputReadingRunnable implements Runnable {
    public StringBuffer buffer;
    private BufferedReader reader;

    public OutputReadingRunnable(InputStream input, StringBuffer buffer) {
      try {
        reader = new BufferedReader(new InputStreamReader(input, DEFAULT_CHARSET_NAME));
      } catch (UnsupportedEncodingException e) {
        exceptionOccurred(e);
      }
      this.buffer = buffer;
    }

    public void run() {
      try {
        int c;
        while ((c = reader.read()) != -1)
          buffer.append((char) c);
      } catch (Exception e) {
        exceptionOccurred(e);
      }
    }

  }

  public int waitForCommandToFinish() throws InterruptedException {
    return process.waitFor();
  }
}