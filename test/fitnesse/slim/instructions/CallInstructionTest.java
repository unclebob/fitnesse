package fitnesse.slim.instructions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class CallInstructionTest {
  private static final String ID = "id_1";
  private static final String RESULT = "result";

  private InstructionExecutor executor;

  @Before
  public void setUp() throws Exception {
    executor = mock(InstructionExecutor.class);

    when(executor.call(anyString(), anyString(), anyVararg())).thenReturn(RESULT);
  }

  @Test
  public void shouldDelegateExecutionToExecutor() throws Exception {
    CallInstruction instruction = new CallInstruction(ID, "instance", "method", new Object[]{"arg1", "arg2"});
    instruction.execute(executor);

    verify(executor, times(1)).call("instance", "method", "arg1", "arg2");
  }

  @Test
  public void shouldFormatReturnValues() {
    CallInstruction instruction = new CallInstruction(ID, "instance", "method", new Object[]{"arg1", "arg2"});

    InstructionResult result = instruction.execute(executor);

    assertEquals(ID, result.getId());
    assertTrue(result.hasResult());
    assertFalse(result.hasError());
    assertEquals(RESULT, result.getResult());
  }
}
