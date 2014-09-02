// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.Arrays;
import java.util.List;

import fitnesse.slim.instructions.ImportInstruction;
import fitnesse.slim.instructions.Instruction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImportTableTest extends SlimTableTestSupport<ImportTable> {

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    String importTableHeader = "|Import|\n";
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
            Arrays.asList(new ImportInstruction("import_id_0", "fitnesse.slim.test"), new ImportInstruction("import_id_1", "x.y.z"));
    assertEquals(expectedInstructions, instructions);
  }
}
