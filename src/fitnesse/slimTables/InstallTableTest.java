package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.List;

import org.junit.Test;

public class InstallTableTest extends SlimTableTestSupport<InstallTable> {
  private String installTableHeader = "|Install|\n";

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    makeSlimTableAndBuildInstructions(installTableHeader + scriptStatements);
  }

  @Test
  public void emptyInstructionsForInstallTable() throws Exception {
    buildInstructionsFor("||\n");
    assertEquals(0, instructions.size());
  }

  @Test
  public void correctInstructionsForInstallTableForOneLibrary() throws Exception {
    buildInstructionsFor("|echo support|\n");
    List<Object> expectedInstructions = list(
        list("install_id_0", "make", "library1", "EchoSupport")
    );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void correctInstructionsForInstallTableForMultipleLibraries() throws Exception {
    buildInstructionsFor("|echo support|\n|file support|\n");
    List<Object> expectedInstructions = list(
        list("install_id_0", "make", "library1", "EchoSupport"),
        list("install_id_1", "make", "library2", "FileSupport")
    );
    assertEquals(expectedInstructions, instructions);
  }
}
