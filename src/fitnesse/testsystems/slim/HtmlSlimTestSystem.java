// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import fitnesse.slim.SlimError;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wikitext.parser.ParsedPage;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class HtmlSlimTestSystem extends SlimTestSystem {
  private HtmlTableScanner tableScanner;
  private SlimTableFactory slimTableFactory = new SlimTableFactory();

  public HtmlSlimTestSystem(String testSystemName, SlimClient slimClient) {
    super(testSystemName, slimClient);
  }

  @Override
  protected void processAllTablesOnPage(TestPage pageToTest) throws IOException {
    List<SlimTable> allTables = createSlimTables(pageToTest);

    if (allTables.size() == 0) {
      String html = createHtmlResults(START_OF_TEST, END_OF_TEST);
      testOutputChunk(html);
    } else {
      for (int index = 0; index < allTables.size(); index++) {
        SlimTable theTable = allTables.get(index);
        SlimTable startWithTable = (index == 0) ? START_OF_TEST : theTable;
        SlimTable nextTable = (index + 1 < allTables.size()) ? allTables.get(index + 1) : END_OF_TEST;

        processTable(theTable);

        String html = createHtmlResults(startWithTable, nextTable);
        testOutputChunk(html);
      }
    }
  }

  private List<SlimTable> createSlimTables(TestPage pageToTest) {
    NodeList nodeList = makeNodeList(pageToTest.getDecoratedData());
    tableScanner = new HtmlTableScanner(nodeList);
    return createSlimTables(tableScanner);
  }

  private NodeList makeNodeList(ReadOnlyPageData pageData) {
    String html = pageData.getHtml();
    Parser parser = new Parser(new Lexer(new Page(html)));
    try {
      return parser.parse(null);
    } catch (ParserException e) {
      throw new SlimError(e);
    }
  }

  private String createHtmlResults(SlimTable startWithTable, SlimTable stopBeforeTable) {
    HtmlTable start = (startWithTable != null) ? (HtmlTable) startWithTable.getTable() : null;
    HtmlTable end = (stopBeforeTable != null) ? (HtmlTable) stopBeforeTable.getTable() : null;
    return tableScanner.toHtml(start, end);
  }

  private List<SlimTable> createSlimTables(TableScanner<? extends Table> tableScanner) {
    List<SlimTable> allTables = new LinkedList<SlimTable>();
    for (Table table : tableScanner)
      createSlimTable(allTables, table);

    return allTables;
  }

  private void createSlimTable(List<SlimTable> allTables, Table table) {
    String tableId = "" + allTables.size();
    SlimTable slimTable = slimTableFactory.makeSlimTable(table, tableId, getTestContext());
    if (slimTable != null) {
      allTables.add(slimTable);
    }
  }
}
