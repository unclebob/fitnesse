// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;

public class ImportTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String importTableHeader = "|Import|\n";
  public ImportTable importTable;
  private MockSlimTestContext testContext;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private ImportTable makeScriptTableAndBuildInstructions(String pageContents) throws Exception {
    importTable = makeImportTable(pageContents);
    importTable.appendInstructions(instructions);
    return importTable;
  }

  private ImportTable makeImportTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    String html = root.getData().getHtml();
    TableScanner ts = new HtmlTableScanner(html);
    Table t = ts.getTable(0);
    testContext = new MockSlimTestContext();
    return new ImportTable(t, "id", testContext);
  }

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    makeScriptTableAndBuildInstructions(importTableHeader + scriptStatements);
  }

  @Test
  public void instructionsForScriptTable() throws Exception {
    buildInstructionsFor("||\n");
    assertEquals(0, instructions.size());
  }

  @Test
  public void importTable() throws Exception {
    buildInstructionsFor(
      "|fitnesse.slim.test|\n" +
        "|x.y.z|\n"
    );
    List<Object> expectedInstructions =
      list(
        list("import_id_0", "import", "fitnesse.slim.test"),
        list("import_id_1", "import", "x.y.z")
      );
    assertEquals(expectedInstructions, instructions);
  }
}
