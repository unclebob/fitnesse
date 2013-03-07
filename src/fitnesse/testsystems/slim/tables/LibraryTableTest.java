package fitnesse.testsystems.slim.tables;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.List;

import fitnesse.slim.instructions.MakeInstruction;
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
    List<MakeInstruction> expectedInstructions = list(
            new MakeInstruction("library_id_0", "library1", "EchoSupport")
    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void correctInstructionsForLibraryTableForMultipleLibraries() throws Exception {
    buildInstructionsFor("|echo support|\n|file support|\n");
    List<MakeInstruction> expectedInstructions = list(
            new MakeInstruction("library_id_0", "library1", "EchoSupport"),
            new MakeInstruction("library_id_1", "library2", "FileSupport")
    );
    assertEquals(expectedInstructions, instructions);
  }
}
