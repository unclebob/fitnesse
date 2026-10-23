// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunningTestingTracker {
  private static final Logger LOG = Logger.getLogger(RunningTestingTracker.class.getName());

  private Map<String, RunningProcess> processes = new ConcurrentHashMap<>();
  private int nextTicketNumber = 1;

  public String generateNextTicket() {
    int ticketNumber;
    synchronized (this) {
      ticketNumber = nextTicketNumber++;
    }
    return Integer.toString(ticketNumber);
  }

  /**
   * @param ticket  String
   * @param process Stoppable process
   * @return id used to identify this process for use with the stop responder
   */
  public synchronized String addStartedProcess(String ticket, Stoppable process) {
    return addStartedProcess(ticket, process, null, null);
  }

  /**
   * @param ticket   String
   * @param process  Stoppable process
   * @param pagePath wiki path of the page being run
   * @param type     kind of run, e.g. "test" or "suite"
   * @return id used to identify this process for use with the stop responder
   */
  public synchronized String addStartedProcess(String ticket, Stoppable process, String pagePath, String type) {
    processes.put(ticket, new RunningProcess(ticket, process, pagePath, type));
    return ticket;
  }

  public synchronized void removeEndedProcess(String stopId) {
    processes.remove(stopId);
  }

  public String stopAllProcesses() {
    int count = 0;
    for (RunningProcess process : processes.values()) {
      stopProcess(process.process);
      count++;
    }
    return "Stopped " + Integer.toString(count) + " test(s) or suite(s)";
  }

  public String stopProcess(String ticket) {
    RunningProcess process = processes.get(ticket);
    if (process != null) {
      stopProcess(process.process);
      return "Stopped 1 test or suite";
    }
    return "Could not find test or suite to stop";
  }

  public boolean isRunning(String ticket) {
    return processes.containsKey(ticket);
  }

  public java.util.Set<String> getRunningIds() {
    return java.util.Collections.unmodifiableSet(new java.util.HashSet<>(processes.keySet()));
  }

  public RunningProcess getRunningProcess(String ticket) {
    return processes.get(ticket);
  }

  public List<RunningProcess> getRunningProcesses() {
    return new ArrayList<>(processes.values());
  }

  private void stopProcess(Stoppable process) {
    try {
      process.stop();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to stop test system", e);
    }
  }

  public static class RunningProcess {
    private final String id;
    private final Stoppable process;
    private final String pagePath;
    private final String type;

    RunningProcess(String id, Stoppable process, String pagePath, String type) {
      this.id = id;
      this.process = process;
      this.pagePath = pagePath;
      this.type = type;
    }

    public String getId() {
      return id;
    }

    public String getPagePath() {
      return pagePath;
    }

    public String getType() {
      return type;
    }
  }
}
