package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

public class CallInstructionTest {
  private static final String ID = "id_1";
  private static final String RESULT = "result";

  private InstructionExecutor executor;
  private NameTranslator nameTranslator;

  @Before
  public void setUp() throws Exception {
    executor = mock(InstructionExecutor.class);
    nameTranslator = mock(NameTranslator.class);

    when(executor.call(anyString(), anyString(), anyVararg())).thenReturn(RESULT);
    when(nameTranslator.translate(anyString())).thenAnswer(returnsFirstArg());
  }

  @Test
  public void shouldTranslateMethodNameOnCreation() {
    new CallInstruction(ID, "instance", "method", new Object[]{"arg1", "arg2"},
      nameTranslator);

    verify(nameTranslator, times(1)).translate("method");
  }

  @Test
  public void shouldDelegateExecutionToExecutor() throws Exception {
    CallInstruction instruction = new CallInstruction(ID, "instance", "method", new Object[]{"arg1", "arg2"},
      nameTranslator);
    instruction.execute(executor);

    verify(executor, times(1)).call("instance", "method", "arg1", "arg2");
  }

  @Test
  public void shouldFormatReturnValues() {
    CallInstruction instruction = new CallInstruction(ID, "instance", "method", new Object[]{"arg1", "arg2"},
      nameTranslator);

    InstructionResult result = instruction.execute(executor);

    assertEquals(ID, result.getId());
    assertTrue(result.hasResult());
    assertFalse(result.hasError());
    assertEquals(RESULT, result.getResult());
  }
}
