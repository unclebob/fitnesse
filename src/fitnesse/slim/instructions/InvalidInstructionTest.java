package fitnesse.slim.instructions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

public class InvalidInstructionTest {

  private InstructionExecutor executor;

  @Before
  public void setUp() {
    this.executor = mock(InstructionExecutor.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldReturnAnErrorResponseOnExecution() {
    InvalidInstruction instruction = new InvalidInstruction("id_1", "invalidFunction");
    InstructionResult result = instruction.execute(executor);
    assertEquals("id_1", result.getId());
    String s = result.getResult().toString();
    assertTrue(s, s.startsWith("__EXCEPTION__:message:<<MALFORMED_INSTRUCTION invalidFunction>>"));
  }
}
