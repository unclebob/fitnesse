// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RunningTestingTrackerTest {
  
  @Test
  public void testAddStoppable() {
    StoppedRecorder stoppableA = new StoppedRecorder();
    StoppedRecorder stoppableB = new StoppedRecorder();
    
    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess(stoppableA);
    tracker.addStartedProcess(stoppableB);
    
    tracker.stopAllProcesses();
    assertTrue(stoppableA.wasStopped());
    assertTrue(stoppableB.wasStopped());
  }
  
  @Test
  public void testRemoveStoppable() {
    StoppedRecorder stoppableA = new StoppedRecorder();
    StoppedRecorder stoppableB = new StoppedRecorder();
    StoppedRecorder stoppableC = new StoppedRecorder();
    
    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess(stoppableA);
    String idB = tracker.addStartedProcess(stoppableB);
    tracker.addStartedProcess(stoppableC);
    
    tracker.removeEndedProcess(idB);
    
    tracker.stopAllProcesses();
    assertTrue(stoppableA.wasStopped());
    assertFalse(stoppableB.wasStopped());
    assertTrue(stoppableC.wasStopped());
  }
  
  
  @Test
  public void testStopProcess() {
    StoppedRecorder stoppableA = new StoppedRecorder();
    StoppedRecorder stoppableB = new StoppedRecorder();
    StoppedRecorder stoppableC = new StoppedRecorder();
    
    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess(stoppableA);
    String idB = tracker.addStartedProcess(stoppableB);
    tracker.addStartedProcess(stoppableC);
    String results = tracker.stopProcess(idB);

    assertFalse(stoppableA.wasStopped());
    assertTrue(stoppableB.wasStopped());
    assertFalse(stoppableC.wasStopped());
    
    assertTrue(results.contains("1"));
  }

  @Test
  public void testStopAllProcesses() {
    StoppedRecorder stoppableA = new StoppedRecorder();
    StoppedRecorder stoppableB = new StoppedRecorder();
    StoppedRecorder stoppableC = new StoppedRecorder();
    
    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess(stoppableA);
    tracker.addStartedProcess(stoppableB);
    tracker.addStartedProcess(stoppableC);

    String results = tracker.stopAllProcesses();

    assertTrue(stoppableA.wasStopped());
    assertTrue(stoppableB.wasStopped());
    assertTrue(stoppableC.wasStopped());
    
    assertTrue(results.contains("3"));
  }

  
  class StoppedRecorder implements Stoppable {
    private boolean wasStopped = false;
    
    public synchronized void stop() throws Exception {
      wasStopped = true;
    }
    
    public synchronized boolean wasStopped() {
      return wasStopped;
    }
  }
}
