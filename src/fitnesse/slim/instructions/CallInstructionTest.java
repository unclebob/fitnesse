package fitnesse.slim.instructions;

import java.util.List;

import fitnesse.slim.NameTranslator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CallInstructionTest {
  private static final String ID = "id_1";
  private static final String RESULT = "result";

  private CallInstruction.CallExecutor executor;
  private NameTranslator nameTranslator;

  @Before
  public void setUp() throws Exception {
    executor = mock(CallInstruction.CallExecutor.class);
    nameTranslator = mock(NameTranslator.class);

    when(executor.call(anyString(), anyString(), anyVararg())).thenReturn(RESULT);
    when(nameTranslator.translate(anyString())).thenAnswer(returnsFirstArg());
  }

  @Test
  public void shouldTranslateMethodNameOnCreation() {
    new CallInstruction(ID, "instance", "method", new Object[] {"arg1", "arg2"},
        nameTranslator);

    verify(nameTranslator, times(1)).translate("method");
  }

  @Test
  public void shouldDelegateExecutionToExecutor() throws Exception {
    CallInstruction instruction = new CallInstruction(ID, "instance", "method", new Object[] {"arg1", "arg2"},
        nameTranslator);
    instruction.execute(executor);

    verify(executor, times(1)).call("instance", "method", "arg1", "arg2");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFormatReturnValues() {
    CallInstruction instruction = new CallInstruction(ID, "instance", "method", new Object[] {"arg1", "arg2"},
        nameTranslator);

    List<Object> result = (List<Object>) instruction.execute(executor);

    assertEquals(ID, result.get(0));
    assertEquals(RESULT, result.get(1));
  }
}
