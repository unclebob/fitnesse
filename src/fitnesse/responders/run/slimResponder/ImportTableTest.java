package fitnesse.responders.run.slimResponder;

import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ImportTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String importTableHeader = "|Import|\n";
  public ImportTable importTable;

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
    TableScanner ts = new WikiTableScanner(root.getData());
    Table t = ts.getTable(0);
    return new ImportTable(t, "id");
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
