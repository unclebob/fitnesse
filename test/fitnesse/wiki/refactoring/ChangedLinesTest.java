// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChangedLinesTest {

  @Test
  public void noChangesForSameContent() {
    assertTrue(ChangedLines.between("a\nb", "a\nb").isEmpty());
  }

  @Test
  public void onlyChangedLinesAreReported() {
    List<ChangedLines> changes = ChangedLines.between("a\nb\nc\nd\ne", "a\nB\nc\nd\nE");

    assertEquals(2, changes.size());
    assertEquals("b", changes.get(0).getBefore());
    assertEquals("B", changes.get(0).getAfter());
    assertEquals("e", changes.get(1).getBefore());
    assertEquals("E", changes.get(1).getAfter());
  }

  @Test
  public void consecutiveChangedLinesAreReportedTogether() {
    List<ChangedLines> changes = ChangedLines.between("a\nb\nc\nd", "a\nB\nC\nd");

    assertEquals(1, changes.size());
    assertEquals("b\nc", changes.get(0).getBefore());
    assertEquals("B\nC", changes.get(0).getAfter());
  }

  @Test
  public void addedAndRemovedLinesHaveEmptyCounterpart() {
    List<ChangedLines> changes = ChangedLines.between("a\nc", "a\nb\nc");

    assertEquals(1, changes.size());
    assertEquals("", changes.get(0).getBefore());
    assertEquals("b", changes.get(0).getAfter());
  }
}
