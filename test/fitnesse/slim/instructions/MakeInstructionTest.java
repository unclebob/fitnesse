package fitnesse.slim.instructions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class MakeInstructionTest {
  private static final String ID = "id_1";

  private final InstructionExecutor executor = mock(InstructionExecutor.class);

  @Test
  public void shouldDelegateCallToExecutor() throws Exception {
    MakeInstruction instruction = new MakeInstruction(ID, "instance", "class", new Object[]{"arg1", "arg2"});

    instruction.execute(executor);

    verify(executor, times(1)).create("instance", "class", "arg1", "arg2");
  }

  @Test
  public void shouldFormatReturnValues() {
    MakeInstruction instruction = new MakeInstruction(ID, "instance", "class", new Object[]{"arg1", "arg2"});

    InstructionResult result = instruction.execute(executor);

    assertEquals(ID, result.getId());
    assertTrue(result.hasResult());
    assertFalse(result.hasError());
    assertEquals("OK", result.getResult());
  }
}
