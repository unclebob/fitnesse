// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

public class HtmlTableListingBuilder {
  private int shade = 0;
  private HtmlTag table;

  public HtmlTableListingBuilder() {
    makeTable();
  }

  public HtmlTag getTable() {
    return table;
  }

  private HtmlTag makeTable() {
    table = new HtmlTag("table");
    table.addAttribute("border", "0");
    table.addAttribute("cellspacing", "0");
    table.addAttribute("class", "dirListing");
    return table;
  }

  public void addRow(HtmlElement[] rowItems) {
    HtmlTag row = new HtmlTag("tr");
    addShade(row);

    HtmlTag cell = null;
    for (HtmlElement rowItem : rowItems) {
      cell = new HtmlTag("td", rowItem);
      row.add(cell);
    }
    cell.addAttribute("style", "text-align: right;");
    table.add(row);
  }

  private void addShade(HtmlTag row) {
    if (shade++ % 2 == 0)
      row.addAttribute("style", "background-color: #EFEFEF;");
    else
      row.addAttribute("style", "background-color: #FFFFFF;");
  }
}
