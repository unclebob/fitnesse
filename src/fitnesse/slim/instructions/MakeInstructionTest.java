package fitnesse.slim.instructions;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MakeInstructionTest {
  private static final String ID = "id_1";
  private static final String RESULT = "OK";

  private MakeInstruction.MakeExecutor executor;

  @Before
  public void setUp() throws Exception {
    executor = mock(MakeInstruction.MakeExecutor.class);

    when(executor.create(anyString(), anyString(), anyVararg())).thenReturn(RESULT);
  }

  @Test
  public void shouldDelegateCallToExecutor() throws Exception {
    MakeInstruction instruction = new MakeInstruction(ID, "instance", "class", new Object[] {"arg1", "arg2"});

    instruction.execute(executor);

    verify(executor, times(1)).create("instance", "class", "arg1", "arg2");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFormatReturnValues() {
    MakeInstruction instruction = new MakeInstruction(ID, "instance", "class", new Object[] {"arg1", "arg2"});

    List<Object> result = (List<Object>) instruction.execute(executor);

    assertEquals(ID, result.get(0));
    assertEquals(RESULT, result.get(1));
  }
}
