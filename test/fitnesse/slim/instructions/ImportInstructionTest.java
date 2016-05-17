package fitnesse.slim.instructions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ImportInstructionTest {
  private static final String ID = "id_1";

  private final InstructionExecutor executor = mock(InstructionExecutor.class);

  @Test
  public void shouldDelegateCallToExecutor() throws Exception {
    ImportInstruction instruction = new ImportInstruction(ID, "path");

    instruction.execute(executor);

    verify(executor, times(1)).addPath("path");
  }

  @Test
  public void shouldFormatReturnValues() {
    ImportInstruction instruction = new ImportInstruction(ID, "path");

    InstructionResult result = instruction.execute(executor);

    assertEquals(ID, result.getId());
    assertFalse(result.hasError());
    assertTrue(result.hasResult());
    assertEquals("OK", result.getResult());
  }
}
