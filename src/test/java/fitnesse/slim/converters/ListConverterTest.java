package fitnesse.slim.converters;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ListConverterTest {
  private ListConverter converter;
  private List<String> result;

  @Before
  public void setup() {

    converter = new ListConverter();
  }

  @Test
  public void fromEmptyList_shouldCreateEmptyList() throws Exception {
    makeList("[]");
    assertEquals(0, result.size());
  }

  @Test
  public void fromEmptyString_shouldCreateEmptyList() throws Exception {
    makeList("");
    assertEquals(0, result.size());
  }

  @Test
  public void fromStringContaingOnlyBlanks_shouldCreateEmptyList() throws Exception {
    makeList(" ");
    assertEquals(0, result.size());
  }

  @SuppressWarnings("unchecked")
  private void makeList(String inputString) {
    result = (List<String>) converter.fromString(inputString);
  }
}
