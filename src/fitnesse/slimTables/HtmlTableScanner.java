// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class HtmlTableScanner implements TableScanner {
  private List<Table> tables = new ArrayList<Table>();
  private NodeList htmlTree;

  public HtmlTableScanner(String page) throws ParserException {
    if (page == null || page.equals(""))
      page = "<i>This page intentionally left blank.</i>";

    Parser parser = new Parser(new Lexer(new Page(page)));
    htmlTree = parser.parse(null);
    scanForTables(htmlTree);
  }

  private void scanForTables(NodeList nodes) {
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.elementAt(i);
      if (node instanceof TableTag) {
        tables.add(new HtmlTable((TableTag) node));
      } else {
        NodeList children = node.getChildren();
        if (children != null)
          scanForTables(children);
      }
    }
  }

  public int getTableCount() {
    return tables.size();
  }

  public Table getTable(int i) {
    return tables.get(i);
  }

  public Iterator<Table> iterator() {
    return tables.iterator();
  }

  public String toWikiText() {
    StringBuffer b = new StringBuffer();
    for (Table t : tables) {
      b.append("\n");
      for (int row = 0; row < t.getRowCount(); row++) {
        b.append("|");
        if (t.getColumnCountInRow(row) == 0)
          b.append("|");
        for (int col = 0; col < t.getColumnCountInRow(row); col++) {
          b.append(t.getCellContents(col, row));
          b.append("|");
        }
        b.append("\n");
      }
    }
    return b.toString();
  }

  public String toHtml() {
    return htmlTree.toHtml();
  }
}
