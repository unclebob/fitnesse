// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import fitnesse.slim.SlimError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HtmlTableScanner implements TableScanner {
  private List<Table> tables = new ArrayList<Table>();
  private NodeList htmlTree;

  public HtmlTableScanner(String page) {
    if (page == null || page.equals(""))
      page = "<i>This page intentionally left blank.</i>";

    try {
      Parser parser = new Parser(new Lexer(new Page(page)));
      htmlTree = parser.parse(null);
    } catch (ParserException e) {
      throw new SlimError(e);
    }
    scanForTables(htmlTree);
  }

  private void scanForTables(NodeList nodes) {
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.elementAt(i);
      if (node instanceof TableTag) {
        TableTag tableTag = (TableTag) node;
        guaranteeThatAllTablesAreUnique(tableTag);
        tables.add(new HtmlTable(tableTag));
      } else {
        NodeList children = node.getChildren();
        if (children != null)
          scanForTables(children);
      }
    }
  }

  private void guaranteeThatAllTablesAreUnique(TableTag tagTable) {
    tagTable.setAttribute("_TABLENUMBER", ""+ Math.abs((new Random()).nextLong()));
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

  public String toHtml(Table startTable, Table endBeforeTable) {
    String allHtml = htmlTree.toHtml();
    
    int startIndex = 0;
    int endIndex = allHtml.length();
    if (startTable != null) {
      String startText = startTable.toHtml();
      int nodeIndex = allHtml.indexOf(startText);
      if (nodeIndex > 0) {
        startIndex = nodeIndex;
      }
    }
    
    if (endBeforeTable != null) {
      String stopText = endBeforeTable.toHtml();
      int nodeIndex = allHtml.indexOf(stopText);
      if (nodeIndex > 0) {
        endIndex = nodeIndex;
      }
    }
    return allHtml.substring(startIndex, endIndex);
  }
  
  public String toHtml() {
    return htmlTree.toHtml();
  }
}
