package fitnesse.slim.instructions;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImportInstructionTest {
  private static final String ID = "id_1";
  private static final String RESULT = "OK";

  private ImportInstruction.ImportExecutor executor;

  @Before
  public void setUp() {
    executor = mock(ImportInstruction.ImportExecutor.class);

    when(executor.addPath(anyString())).thenReturn(RESULT);
  }

  @Test
  public void shouldDelegateCallToExecutor() {
    ImportInstruction instruction = new ImportInstruction(ID, "path");

    instruction.execute(executor);

    verify(executor, times(1)).addPath("path");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFormatReturnValues() {
    ImportInstruction instruction = new ImportInstruction(ID, "path");

    List<Object> result = (List<Object>) instruction.execute(executor);

    assertEquals(ID, result.get(0));
    assertEquals(RESULT, result.get(1));
  }
}
