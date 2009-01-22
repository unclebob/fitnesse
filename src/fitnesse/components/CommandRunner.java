// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.components;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
  private long startTime;
  private long endTime;
  private String command = "";

  public CommandRunner() {
  }

  public CommandRunner(String command, String input) {
    this.command = command;
    this.input = input;
  }

  public void asynchronousStart() throws Exception {
    Runtime rt = Runtime.getRuntime();
    startTime = System.currentTimeMillis();
    process = rt.exec(command);
    stdin = process.getOutputStream();
    stdout = process.getInputStream();
    stderr = process.getErrorStream();

    new Thread(new OuputReadingRunnable(stdout, outputBuffer), "CommandRunner stdout").start();
    new Thread(new OuputReadingRunnable(stderr, errorBuffer), "CommandRunner error").start();

    sendInput();
  }

  public void run() throws Exception {
    asynchronousStart();
    join();
  }

  public void join() throws Exception {
    process.waitFor();
    endTime = System.currentTimeMillis();
    exitCode = process.exitValue();
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
    return endTime - startTime;
  }

  protected void sendInput() throws Exception {
    Thread thread = new Thread() {
      public void run() {
        try {
          stdin.write(input.getBytes("UTF-8"));
          stdin.flush();
        }
        catch (Exception e) {
          exceptionOccurred(e);
        }
        finally {
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
    }
    catch (Exception e) {
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
