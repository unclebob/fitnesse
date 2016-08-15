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

public class CallAndAssignInstructionTest {
  private static final String RESULT = "result";

  private InstructionExecutor executor;

  @Before
  public void setUp() throws Exception {
    executor = mock(InstructionExecutor.class);

    when(executor.callAndAssign(anyString(), anyString(), anyString(), anyVararg())).thenReturn(RESULT);
  }

  @Test
  public void shouldCallExecutorOnExecution() throws Exception {
    CallAndAssignInstruction instruction = new CallAndAssignInstruction("id_1", "symbol", "instance", "method",
        new Object[] {"arg1", "arg2"});

    instruction.execute(executor);

    verify(executor, times(1)).callAndAssign("symbol", "instance", "method", "arg1", "arg2");
  }

  @Test
  public void shouldReturnExecutionResults() {
    CallAndAssignInstruction instruction = new CallAndAssignInstruction("id_1", "symbol", "instance", "method",
        new Object[] {"arg1", "arg2"});

    InstructionResult result = instruction.execute(executor);

    assertEquals("id_1", result.getId());
    assertTrue(result.hasResult());
    assertFalse(result.hasError());
    assertEquals(RESULT, result.getResult());
  }
}
