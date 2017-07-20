// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public class ColumnFixture extends Fixture {
  protected Binding[] columnBindings;
  protected boolean executeCalledForRow = false;

  @Override
  public void doRows(Parse rows) {
    bindColumnHeadersToMethodsAndFields(rows.parts);
    super.doRows(rows.more);
  }

  @Override
  public void doRow(Parse row) {
    executeCalledForRow = false;
    try {
      reset();
      super.doRow(row);
      if (!executeCalledForRow)
        execute();
    } catch (Exception e) {
      exception(row.leaf(), e);
    }
  }

  @Override
  public void doCell(Parse cell, int column) {
    try {
      columnBindings[column].doCell(this, cell);
    } catch (Throwable e) { // NOSONAR
      exception(cell, e);
    }
  }

  @Override
  public void check(Parse cell, TypeAdapter a) {
    try {
      executeIfNeeded();
    } catch (Exception e) {
      exception(cell, e);
    }
    super.check(cell, a);
  }

  protected void executeIfNeeded() throws Exception {
    if (!executeCalledForRow) {
      executeCalledForRow = true;
      execute();
    }
  }

  public void reset() throws Exception {
  }

  public void execute() throws Exception {
  }

  protected void bindColumnHeadersToMethodsAndFields(Parse heads) {
    try {
      columnBindings = new Binding[heads.size()];
      for (int i = 0; heads != null; i++, heads = heads.more) {
        columnBindings[i] = createBinding(i, heads);
      }
    } catch (Throwable throwable) { // NOSONAR
      exception(heads, throwable);
    }
  }

  protected Binding createBinding(int column, Parse heads) throws Throwable {
    return Binding.create(this, heads.text());
  }
}
