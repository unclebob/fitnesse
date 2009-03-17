// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.revisioncontrol.svn;

import static fitnesse.revisioncontrol.RevisionControlOperation.ADD;
import static fitnesse.revisioncontrol.RevisionControlOperation.CHECKIN;
import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;
import static fitnesse.revisioncontrol.RevisionControlOperation.REVERT;
import static fitnesse.revisioncontrol.RevisionControlOperation.UPDATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fitnesse.revisioncontrol.RevisionControlOperation;

public class SVNStateTest {

  @Test
  public void canPerformAddOperationIfStateIsUnknown() throws Exception {
    final RevisionControlOperation[] operations = SVNState.UNKNOWN.operations();
    assertEquals("Only 1 operation should be allowed in Unknown state", 1, operations.length);
    assertEquals(ADD, operations[0]);
  }

  @Test
  public void canPerformCheckInUpdateRevertAndDeleteOperationsIfStateIsVersioned() throws Exception {
    final RevisionControlOperation[] operations = SVNState.VERSIONED.operations();
    assertEquals("Only 4 operations should be allowed in Versioned state", 4, operations.length);
    assertContains(operations, CHECKIN, UPDATE, REVERT, DELETE);
  }

  @Test
  public void canPerformCheckInAndRevertOperationsIfStateIsDeleted() throws Exception {
    final RevisionControlOperation[] operations = SVNState.DELETED.operations();
    assertEquals("Only 2 operations should be allowed in Versioned state", 2, operations.length);
    assertContains(operations, CHECKIN, REVERT);
  }

  @Test
  public void canPerformCheckInAndRevertOperationsIfStateIsAdded() throws Exception {
    final RevisionControlOperation[] operations = SVNState.ADDED.operations();
    assertEquals("Only 2 operations should be allowed in Versioned state", 2, operations.length);
    assertContains(operations, CHECKIN, REVERT);
  }

  @Test
  public void testIsNotUnderRevisionControl() throws Exception {
    assertTrue("Files in Unknown State should not be under revision control", SVNState.UNKNOWN.isNotUnderRevisionControl());
    assertFalse("Files in Checked In State should be under revision control", SVNState.VERSIONED.isNotUnderRevisionControl());
    assertTrue("Files in Added State should not be under revision control", SVNState.ADDED.isNotUnderRevisionControl());
    assertFalse("Files in Deleted State should be under revision control", SVNState.DELETED.isNotUnderRevisionControl());
  }

  @Test
  public void testIsCheckedIn() throws Exception {
    assertTrue("Files in Checked In State should be checked in", SVNState.VERSIONED.isCheckedIn());
    assertFalse("Files in Unknown State should not be checked in", SVNState.UNKNOWN.isCheckedIn());
    assertTrue("Files in Deleted State should be checked in", SVNState.DELETED.isCheckedIn());
    assertFalse("Files in Added State should not be checked in", SVNState.ADDED.isCheckedIn());
  }

  @Test
  public void testIsCheckedOut() throws Exception {
    assertTrue("Versioned Files should be checked out", SVNState.VERSIONED.isCheckedOut());
    assertFalse("Files in Unknown State should not be checked out", SVNState.UNKNOWN.isCheckedOut());
    assertTrue("Files in Deleted State should be checked out", SVNState.DELETED.isCheckedOut());
    assertTrue("Files in Added State should not be checked out", SVNState.ADDED.isCheckedOut());
  }

  private void assertContains(RevisionControlOperation[] operations, RevisionControlOperation... expectedOperations) {
    final List<RevisionControlOperation> ops = Arrays.asList(operations);
    for (final RevisionControlOperation operation : expectedOperations)
      assertTrue(ops.contains(operation));
  }
}
