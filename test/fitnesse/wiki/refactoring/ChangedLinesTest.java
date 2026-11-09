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
  public void changedWordsAreMarked() {
    ChangedLines change = new ChangedLines("|enter|userName|with|bob|", "|enter|loginName|with|bob|");

    assertEquals("|enter|<mark>userName</mark>|with|bob|", change.getBeforeHtml());
    assertEquals("|enter|<mark>loginName</mark>|with|bob|", change.getAfterHtml());
  }

  @Test
  public void addedWordsAreOnlyMarkedAfter() {
    ChangedLines change = new ChangedLines("|go to|url|", "|go to|url|now|");

    assertEquals("|go to|url|", change.getBeforeHtml());
    assertEquals("|go to|url|<mark>now|</mark>", change.getAfterHtml());
  }

  @Test
  public void markedLinesAreEscaped() {
    ChangedLines change = new ChangedLines("a < b", "a <= b");

    assertEquals("a &lt; b", change.getBeforeHtml());
    assertEquals("a &lt;<mark>=</mark> b", change.getAfterHtml());
  }

  @Test
  public void addedAndRemovedLinesHaveEmptyCounterpart() {
    List<ChangedLines> changes = ChangedLines.between("a\nc", "a\nb\nc");

    assertEquals(1, changes.size());
    assertEquals("", changes.get(0).getBefore());
    assertEquals("b", changes.get(0).getAfter());
  }
}
