package fitnesse.wiki;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WikiPageActionTest {
  private WikiPageAction a;
  private WikiPageAction b;
  private WikiPageAction aa;


  @Before
  public void before() {
    a = new WikiPageAction("a", "a");
    b = new WikiPageAction("b", "b");
    aa = new WikiPageAction("a", "a");
  }

  @Test
  public void objectEqualsSelf() throws Exception {
    assertTrue(a.equals(a));
  }

  @Test
  public void unequalValuesNotEqual() throws Exception {
    assertFalse(a.equals(b));
  }

  @Test
  public void equalValuesAreEqual() throws Exception {
    assertTrue(a.equals(aa));
  }

  @Test
  public void notEqualNull() throws Exception {
    assertFalse(a.equals(null));
  }

  @Test
  public void notEqualDifferentClass() throws Exception {
    String x = "";
    assertFalse(a.equals(x));
  }

  @Test
  public void windowDifferent() throws Exception {
    aa.setNewWindow(!a.isNewWindow());
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void linkNameDifferent() throws Exception {
    aa.setLinkName("b");
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void linkNameNull() throws Exception {
    aa.setLinkName(null);
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void bothLinkNamesNull() throws Exception {
    aa.setLinkName(null);
    a.setLinkName(null);
    assertTrue(a.equals(aa)); 
  }

  @Test
  public void pageNameDifferent() throws Exception {
    aa.setPageName("b");
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void pageNameNull() throws Exception {
    aa.setPageName(null);
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void bothPageNamesNull() throws Exception {
    aa.setPageName(null);
    a.setPageName(null);
    assertTrue(a.equals(aa));
  }

  @Test
  public void queryDifferent() throws Exception {
    aa.setQuery("b");
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void queryNull() throws Exception {
    aa.setQuery(null);
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void bothQuerysNull() throws Exception {
    aa.setQuery(null);
    a.setQuery(null);
    assertTrue(a.equals(aa));
  }

  @Test
  public void shortcutKeyDifferent() throws Exception {
    aa.setShortcutKey("b");
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void shortcutKeyNull() throws Exception {
    aa.setShortcutKey(null);
    assertFalse(aa.equals(a));
    assertFalse(a.equals(aa));
  }

  @Test
  public void bothShortcutKeysNull() throws Exception {
    aa.setShortcutKey(null);
    a.setShortcutKey(null);
    assertTrue(a.equals(aa));
  }

  @Test
  public void hashWithAllStringsLoadedDoesNotThrow() throws Exception {
    a.hashCode();
  }

  @Test
  public void hashWithNullsDoesNotThrow() throws Exception {
    a.setLinkName(null);
    a.setPageName(null);
    a.setQuery(null);
    a.setShortcutKey(null);
    a.setNewWindow(!aa.isNewWindow());
    a.hashCode();
  }
}
