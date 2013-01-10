package fitnesse.slim.instructions;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

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
    List<Object> result = (List<Object>) instruction.execute(executor);
    assertEquals("id_1", result.get(0));
    assertEquals("__EXCEPTION__:message:<<INVALID_STATEMENT: invalidFunction>>", result.get(1));
  }
}
