// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.testsystems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import static java.util.Arrays.asList;
import static util.FileUtil.CHARENCODING;

public class CommandRunner {
  private static final Logger LOG = Logger.getLogger(CommandRunner.class.getName());

  private Process process;
  private String input = "";
  protected int exitCode = -1;
  private String[] command;
  private Map<String, String> environmentVariables;
  private final int timeout;
  private final ExecutionLogListener executionLogListener;

  /**
   *
   * @param command Commands to run
   * @param input INput
   * @param environmentVariables Map of environment variables
   * @param executionLogListener Execution Log Listener
   * @param timeout Time-out in seconds.
   */
  public CommandRunner(String[] command, String input, Map<String, String> environmentVariables, ExecutionLogListener executionLogListener, int timeout) {
    if (executionLogListener == null) {
      throw new IllegalArgumentException("executionLogListener may not be null");
    }
    this.command = command;
    this.input = input;
    this.environmentVariables = environmentVariables;
    this.executionLogListener = executionLogListener;
    this.timeout = timeout;
  }

  public CommandRunner(String[] command, String input, Map<String, String> environmentVariables, ExecutionLogListener executionLogListener) {
    this(command, input, environmentVariables, executionLogListener, 2);
  }

  public void asynchronousStart() throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.environment().putAll(determineEnvironment());
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Starting process " + asList(command));
    }
    process = processBuilder.start();

    OutputStream stdin = process.getOutputStream();
    InputStream stdout = process.getInputStream();
    InputStream stderr = process.getErrorStream();

    sendCommandStartedEvent();
    new Thread(new OutputReadingRunnable(stdout, new OutputWriter() {
      @Override
      public void write(String output) {
        executionLogListener.stdOut(output);
      }
    }), "CommandRunner stdOut").start();

    new Thread(new OutputReadingRunnable(stderr, new OutputWriter() {
      @Override
      public void write(String output) {
        executionLogListener.stdErr(output);
      }
    }), "CommandRunner stdErr").start();

    sendInput(stdin);
  }

  protected void sendCommandStartedEvent() {
    executionLogListener.commandStarted(new ExecutionLogListener.ExecutionContext() {
      @Override
      public String getCommand() {
        return StringUtils.join(asList(command), " ");
      }

      @Override
      public String getTestSystemName() {
        // What to do with this? Need an identifier?
        return "command";
      }
    });
  }

  private Map<String, String> determineEnvironment() {
    if (environmentVariables == null) {
      return Collections.emptyMap();
    }
    Map<String, String> systemVariables = new HashMap<String, String>(System.getenv());
    systemVariables.putAll(environmentVariables);
    return systemVariables;
  }

  public void join() {
    if (process != null) {
      waitForDeathOf(process);
      if (isDead(process)) {
        exitCode = process.exitValue();
        executionLogListener.exitCode(exitCode);

      }
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

  public boolean isDead() {
	  if (process !=null) return isDead(process);
	  //if there is or was never a process due to a remote / manual start then it is alive!
	  return false;
  }

  public void kill() {
    if (process != null) {
      process.destroy();
      join();
    }
  }

  public String[] getCommand() {
    return command;
  }

  public String getOutput() {
    return "";
  }

  public String getError() {
    return "";
  }

  public List<Throwable> getExceptions() {
    return Collections.emptyList();
  }

  @Deprecated
  public int getExitCode() {
    return exitCode;
  }

  // Used to catch exceptions thrown from the read and write threads.
  public void exceptionOccurred(Exception e) {
    executionLogListener.exceptionOccurred(e);
  }

  protected void sendInput(OutputStream stdin) throws IOException {
    try {
      stdin.write(input.getBytes(CHARENCODING));
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
    public OutputWriter writer;
    private BufferedReader reader;

    public OutputReadingRunnable(InputStream input, OutputWriter writer) {
      try {
        reader = new BufferedReader(new InputStreamReader(input, CHARENCODING));
      } catch (UnsupportedEncodingException e) {
        exceptionOccurred(e);
      }
      this.writer = writer;
    }

    @Override
    public void run() {
      try {
        String s;
        while ((s = reader.readLine()) != null) {
          writer.write(s);
        }
      } catch (Exception e) {
        exceptionOccurred(e);
      }
    }

  }

  public int waitForCommandToFinish() throws InterruptedException {
    return process.waitFor();
  }

  private interface OutputWriter {
    void write(String output);
  }
}