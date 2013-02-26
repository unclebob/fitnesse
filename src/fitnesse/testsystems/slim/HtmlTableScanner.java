// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import static fitnesse.util.HtmlParserTools.deepClone;
import static fitnesse.util.HtmlParserTools.flatClone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import fitnesse.slim.SlimError;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

// TODO: TableScanner should return a list of Tables and page content nodes
// TODO: Need logic to split (by clone) data blocks and and to render (to html) those blocks
public class HtmlTableScanner implements TableScanner<HtmlTable> {

  // This should contain content blobs (List<Object>?)
  private List<HtmlTable> tables = new ArrayList<HtmlTable>(16);
  // TODO: use NodeList instead?
  private List<Node> nodes = new ArrayList<Node>(512);

  public HtmlTableScanner(String page) {
    if (page == null || page.equals(""))
      page = "<i>This page intentionally left blank.</i>";

    NodeList htmlTree;
    try {
      Parser parser = new Parser(new Lexer(new Page(page)));
      htmlTree = parser.parse(null);
    } catch (ParserException e) {
      throw new SlimError(e);
    }
    scanForTables(htmlTree);
  }

  public HtmlTableScanner(String... fragments) {
    NodeList htmlTree;
    try {
      htmlTree = new NodeList();
      for (String fragment: fragments) {
        Parser parser = new Parser(new Lexer(new Page(fragment)));
        NodeList tree = parser.parse(null);
        htmlTree.add(tree);
      }
    } catch (ParserException e) {
      throw new SlimError(e);
    }
    scanForTables(htmlTree);
  }

  public HtmlTableScanner(NodeList... nodeLists) {
    for (NodeList nodeList: nodeLists) {
      scanForTables(nodeList);
    }
  }

  private void scanForTables(NodeList nodes) {
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.elementAt(i);
      if (node instanceof TableTag) {
        TableTag tableTag = deepClone((TableTag) node);
        guaranteeThatAllTablesAreUnique(tableTag);
        tables.add(new HtmlTable(tableTag));
        this.nodes.add(tableTag);
      } else {
        Node newNode = flatClone(node);
        this.nodes.add(newNode);
        NodeList children = node.getChildren();
        if (children != null) {
          scanForTables(children);
        }

        if (needEndTag(node)) {
          // No copying required since the node is not modified and has no children.
          this.nodes.add(((Tag) node).getEndTag());
          ((Tag) newNode).setEndTag(null);
        }
      }
    }
  }

  private boolean needEndTag(Node node) {
    return node instanceof Tag && !((Tag) node).isEmptyXmlTag() && ((Tag) node).getEndTag() != null;
  }

  private void guaranteeThatAllTablesAreUnique(TableTag tagTable) {
    tagTable.setAttribute("_TABLENUMBER", ""+ Math.abs((new Random()).nextLong()), '"');
  }

  public int getTableCount() {
    return tables.size();
  }

  public HtmlTable getTable(int i) {
    return tables.get(i);
  }

  public Iterator<HtmlTable> iterator() {
    return tables.iterator();
  }

  public String toHtml(HtmlTable startTable, HtmlTable endBeforeTable) {

    int index = 0;
    if (startTable != null) {
      index = nodes.indexOf(startTable.getTableNode());
    }

    Node endTag = null;
    if (endBeforeTable != null) {
      endTag = endBeforeTable.getTableNode();
    }

    StringBuilder html = new StringBuilder(512);
    for (int i = index; i < nodes.size() && nodes.get(i) != endTag; i++) {
      Node node = nodes.get(i);
      html.append(node.toHtml());
    }

    return html.toString();
  }

  public String toHtml() {
    return toHtml(null, null);
  }
}
