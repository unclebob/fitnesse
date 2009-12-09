package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.List;

import org.junit.Test;

public class LibraryTableTest extends SlimTableTestSupport<LibraryTable> {
  private String tableHeader = "|Library|\n";

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    makeSlimTableAndBuildInstructions(tableHeader + scriptStatements);
  }

  @Test
  public void emptyInstructionsForLibraryTable() throws Exception {
    buildInstructionsFor("||\n");
    assertEquals(0, instructions.size());
  }

  @Test
  public void correctInstructionsForLibraryTableForOneLibrary() throws Exception {
    buildInstructionsFor("|echo support|\n");
    List<Object> expectedInstructions = list(
        list("library_id_0", "make", "library1", "EchoSupport")
    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void correctInstructionsForLibraryTableForMultipleLibraries() throws Exception {
    buildInstructionsFor("|echo support|\n|file support|\n");
    List<Object> expectedInstructions = list(
        list("library_id_0", "make", "library1", "EchoSupport"),
        list("library_id_1", "make", "library2", "FileSupport")
    );
    assertEquals(expectedInstructions, instructions);
  }
}
