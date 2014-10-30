package fitnesse.slim.instructions;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AssignInstructionTest {
  private static final String ID = "id_1";

  private InstructionExecutor executor;

  @Before
  public void setUp() throws Exception {
    executor = mock(InstructionExecutor.class);
  }

  @Test
  public void shouldDelegateSetVariableToExecutor() throws Exception {
    AssignInstruction instruction = new AssignInstruction(ID, "symbolName", "value");

    instruction.execute(executor);

    verify(executor, times(1)).assign("symbolName", "value");
  }

  @Test
  public void shouldFormatReturnValues() {
    AssignInstruction instruction = new AssignInstruction(ID, "symbolName", "value");

    InstructionResult result = instruction.execute(executor);

    assertEquals(ID, result.getId());
    assertTrue(result.hasResult());
    assertFalse(result.hasError());
    assertEquals("OK", result.getResult());
  }
}
