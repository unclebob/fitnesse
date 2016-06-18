// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.util.LinkedList;
import java.util.List;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import fitnesse.wiki.PageData;
import fitnesse.slim.SlimError;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.testsystems.slim.tables.SyntaxError;

public class HtmlSlimTestSystem extends SlimTestSystem {


  private static final SlimTable START_OF_TEST = null;
  private static final SlimTable END_OF_TEST = null;

  private final SlimTableFactory slimTableFactory;
  private final CustomComparatorRegistry customComparatorRegistry;
  private HtmlTableScanner tableScanner;

  public HtmlSlimTestSystem(String testSystemName, SlimClient slimClient,
                            SlimTableFactory slimTableFactory,
                            CustomComparatorRegistry customComparatorRegistry) {
    super(testSystemName, slimClient);
    this.slimTableFactory = slimTableFactory;
    this.customComparatorRegistry = customComparatorRegistry;
  }

  @Override
  protected void processAllTablesOnPage(TestPage pageToTest) throws TestExecutionException {
    List<SlimTable> allTables = createSlimTables(pageToTest);

    boolean isSuiteTearDownPage = PageData.SUITE_TEARDOWN_NAME.equals(pageToTest.getName());

    if (allTables.isEmpty()) {
      String html = createHtmlResults(START_OF_TEST, END_OF_TEST);
      testOutputChunk(html);
    } else {
      for (int index = 0; index < allTables.size(); index++) {
        SlimTable theTable = allTables.get(index);
        SlimTable startWithTable = (index == 0) ? START_OF_TEST : theTable;
        SlimTable nextTable = (index + 1 < allTables.size()) ? allTables.get(index + 1) : END_OF_TEST;

        try {
          processTable(theTable, isSuiteTearDownPage);
        } catch (SyntaxError e) {
          String tableName = theTable.getTable().getCellContents(0, 0);
          theTable.getTable().updateContent(0, 0, SlimTestResult.error(String.format("<strong> %s: Bad table! %s</strong>", tableName, e.getMessage())));
          getTestContext().incrementErroredTestsCount();
        }

        String html = createHtmlResults(startWithTable, nextTable);
        testOutputChunk(html);
      }
    }
  }

  private List<SlimTable> createSlimTables(TestPage pageToTest) {
    NodeList nodeList = makeNodeList(pageToTest);
    tableScanner = new HtmlTableScanner(nodeList);
    return createSlimTables(tableScanner);
  }

  private NodeList makeNodeList(TestPage pageToTest) {
    String html = pageToTest.getHtml();
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
    List<SlimTable> allTables = new LinkedList<>();
    for (Table table : tableScanner)
      createSlimTable(allTables, table);

    return allTables;
  }

  private void createSlimTable(List<SlimTable> allTables, Table table) {
    String tableId = "" + allTables.size();
    SlimTable slimTable = slimTableFactory.makeSlimTable(table, tableId, getTestContext());
    if (slimTable != null) {
      slimTable.setCustomComparatorRegistry(customComparatorRegistry);
      allTables.add(slimTable);
    }
  }
}
