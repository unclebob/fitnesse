// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fitnesse.slim.SlimError;
import fitnesse.wikitext.parser.Include;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import static fitnesse.util.HtmlParserTools.*;

public class HtmlTableScanner implements TableScanner<HtmlTable> {

  private List<HtmlTable> tables = new ArrayList<>(16);
  private List<Node> nodes = new ArrayList<>(512);

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

  public HtmlTableScanner(NodeList... nodeLists) {
    for (NodeList nodeList: nodeLists) {
      scanForTables(nodeList);
    }
  }

  private void scanForTables(NodeList nodes) {
    scanForTables(nodes, false);
  }

  private void scanForTables(NodeList nodes, boolean markAsTeardown) {
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.elementAt(i);
      if (node instanceof TableTag) {
        TableTag tableTag = deepClone((TableTag) node);
        HtmlTable htmlTable = new HtmlTable(tableTag);
        htmlTable.setTearDown(markAsTeardown);
        tables.add(htmlTable);
        this.nodes.add(tableTag);
      } else {
        this.nodes.add(flatClone(node));

        NodeList children = node.getChildren();
        if (children != null) {
          scanForTables(children, markAsTeardown || nodeHasClass(node, Include.TEARDOWN));
        }

        Node endNode = endTag(node);
        if (endNode != null) {
          this.nodes.add(endNode);
        }
      }
    }
  }

  @Override
  public int getTableCount() {
    return tables.size();
  }

  @Override
  public HtmlTable getTable(int i) {
    return tables.get(i);
  }

  @Override
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
