// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import static org.junit.Assert.assertEquals;
import static util.ListUtility.list;

import java.util.List;

import fitnesse.slim.instructions.ImportInstruction;
import fitnesse.slim.instructions.Instruction;
import org.junit.Test;

public class ImportTableTest extends SlimTableTestSupport<ImportTable> {
  private final String importTableHeader = "|Import|\n";

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    makeSlimTableAndBuildInstructions(importTableHeader + scriptStatements);
  }

  @Test
  public void instructionsForImportTable() throws Exception {
    buildInstructionsFor("||\n");
    assertEquals(0, instructions.size());
  }

  @Test
  public void importTable() throws Exception {
    buildInstructionsFor(
      "|fitnesse.slim.test|\n" +
        "|x.y.z|\n"
    );
    List<? extends Instruction> expectedInstructions =
      list(
              new ImportInstruction("import_id_0", "fitnesse.slim.test"),
              new ImportInstruction("import_id_1", "x.y.z")
      );
    assertEquals(expectedInstructions, instructions);
  }
}
