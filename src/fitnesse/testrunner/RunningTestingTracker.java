// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.testrunner.Stoppable;

public class RunningTestingTracker implements TestingTracker {
  public static final Logger LOG = Logger.getLogger(RunningTestingTracker.class.getName());

  private HashMap<String, Stoppable> processes = new HashMap<String, Stoppable>();
  private int nextTicketNumber = 1;

  /**
   * @param process
   * @return id used to identify this process for use with the stop responder
   */
  public synchronized String addStartedProcess(Stoppable process) {
    int ticketNumber;
    synchronized (this) {
      ticketNumber = nextTicketNumber++;
    }
    String ticket = Integer.toString(ticketNumber);
    processes.put(ticket, process);
    return ticket;
  }

  public synchronized void removeEndedProcess(String stopId) {
    processes.remove(stopId);
  }

  public String stopAllProcesses() {
    int count = 0;
    for (Stoppable process : processes.values()) {
      stopProcess(process);
      count++;
    }
    return "Stopped " + Integer.toString(count) + " test(s) or suite(s)";
  }

  public String stopProcess(String ticket) {
    Stoppable process = processes.get(ticket);
    if (process != null) {
      stopProcess(process);
      return "Stopped 1 test or suite";
    }
    return "Could not find test or suite to stop";
  }

  private void stopProcess(Stoppable process) {
    try {
      process.stop();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to stop test system", e);
    }
  }

}
