// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fitnesse.testrunner.RunningTestingTracker.RunningProcess;
import org.junit.Test;

public class RunningTestingTrackerTest {
  
  @Test
  public void testAddStoppable() {
    StoppedRecorder stoppableA = new StoppedRecorder();
    StoppedRecorder stoppableB = new StoppedRecorder();
    
    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess("1", stoppableA);
    tracker.addStartedProcess("2", stoppableB);
    
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
    tracker.addStartedProcess("1", stoppableA);
    String idB = tracker.addStartedProcess("2", stoppableB);
    tracker.addStartedProcess("3", stoppableC);
    
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
    tracker.addStartedProcess("1", stoppableA);
    String idB = tracker.addStartedProcess("2", stoppableB);
    tracker.addStartedProcess("3", stoppableC);
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
    tracker.addStartedProcess("1", stoppableA);
    tracker.addStartedProcess("2", stoppableB);
    tracker.addStartedProcess("3", stoppableC);

    String results = tracker.stopAllProcesses();

    assertTrue(stoppableA.wasStopped());
    assertTrue(stoppableB.wasStopped());
    assertTrue(stoppableC.wasStopped());
    
    assertTrue(results.contains("3"));
  }

  @Test
  public void testStopUnknownProcess() {
    StoppedRecorder stoppableA = new StoppedRecorder();

    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess("1", stoppableA);
    String results = tracker.stopProcess("42");

    assertFalse(stoppableA.wasStopped());
    assertEquals("Could not find test or suite to stop", results);
  }

  @Test
  public void testStopAllProcessesContinuesAfterFailingStop() {
    StoppedRecorder stoppableB = new StoppedRecorder();

    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess("1", () -> { throw new IllegalStateException("cannot stop"); });
    tracker.addStartedProcess("2", stoppableB);

    String results = tracker.stopAllProcesses();

    assertTrue(stoppableB.wasStopped());
    assertTrue(results.contains("2"));
  }

  @Test
  public void testRunningProcessesKeepPathAndType() {
    RunningTestingTracker tracker = new RunningTestingTracker();
    tracker.addStartedProcess("1", new StoppedRecorder(), "FrontPage.MySuite", "suite");
    tracker.addStartedProcess("2", new StoppedRecorder(), "FrontPage.MyTest", "test");
    tracker.addStartedProcess("3", new StoppedRecorder());

    RunningProcess process = tracker.getRunningProcess("2");
    assertEquals("2", process.getId());
    assertEquals("FrontPage.MyTest", process.getPagePath());
    assertEquals("test", process.getType());

    process = tracker.getRunningProcess("3");
    assertNull(process.getPagePath());
    assertNull(process.getType());

    assertNull(tracker.getRunningProcess("42"));
    assertEquals(3, tracker.getRunningProcesses().size());
  }


  class StoppedRecorder implements Stoppable {
    private boolean wasStopped = false;
    
    @Override
    public synchronized void stop() {
      wasStopped = true;
    }
    
    public synchronized boolean wasStopped() {
      return wasStopped;
    }
  }
}
