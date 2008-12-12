package fitnesse.responders.run.slimResponder;

import java.util.Iterator;

public interface TableScanner extends Iterable<Table> {
  public int getTableCount();

  public Table getTable(int i);

  public Iterator<Table> iterator();

  public String toWikiText();

  public String toHtml();
}
