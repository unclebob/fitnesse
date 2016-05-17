package fitnesse.slim.instructions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class InvalidInstructionTest {

  private final InstructionExecutor executor= mock(InstructionExecutor.class);

  @Test
  public void shouldReturnAnErrorResponseOnExecution() {
    InvalidInstruction instruction = new InvalidInstruction("id_1", "invalidFunction");
    InstructionResult result = instruction.execute(executor);
    assertEquals("id_1", result.getId());
    String s = result.getResult().toString();
    assertTrue(s, s.startsWith("__EXCEPTION__:message:<<MALFORMED_INSTRUCTION invalidFunction>>"));
  }
}
