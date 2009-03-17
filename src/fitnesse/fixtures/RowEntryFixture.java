// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import java.io.PrintWriter;
import java.io.StringWriter;

import fit.ColumnFixture;
import fit.Parse;

public abstract class RowEntryFixture extends ColumnFixture {
  public abstract void enterRow() throws Exception;

  public final static String ERROR_INDICATOR = "Unable to enter last row: ";
  public static final String RIGHT_STYLE = "pass";
  public static final String WRONG_STYLE = "fail";

  public void doRow(Parse row) {
    if (row.parts.body.indexOf(ERROR_INDICATOR) != -1)
      return;
    super.doRow(row);
    try {
      enterRow();
      right(appendCell(row, "entered"));
    }
    catch (Exception e) {
      wrong(appendCell(row, "skipped"));
      reportError(row, e);
    }
  }

  protected Parse appendCell(Parse row, String text) {
    Parse lastCell = new Parse("td", text, null, null);
    row.parts.last().more = lastCell;
    return lastCell;
  }

  public void reportError(Parse row, Exception e) {
    Parse errorCell = makeMessageCell(e);
    insertRowAfter(row, new Parse("tr", null, errorCell, null));
  }

  public Parse makeMessageCell(Exception e) {
    Parse errorCell = new Parse("td", "", null, null);
    final StringWriter buffer = new StringWriter();

    e.printStackTrace(new PrintWriter(buffer));
    errorCell.addToTag(" colspan=\"" + (columnBindings.length + 1) + "\"");
    errorCell.addToBody("<i>" + ERROR_INDICATOR + e.getMessage() + "</i>");
    errorCell.addToBody("<pre>" + (buffer.toString()) + "</pre>");
    wrong(errorCell);

    return errorCell;
  }

  public void insertRowAfter(Parse currentRow, Parse rowToAdd) {
    Parse nextRow = currentRow.more;
    currentRow.more = rowToAdd;
    rowToAdd.more = nextRow;
  }

}
