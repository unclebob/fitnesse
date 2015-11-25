package fitnesse.wikitext.parser;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RandomNumberTest
{
  private static int MAX_ITERATIONS = 1000000; // Used for sanity check

  @Test
  public void translatesRandomNumbers()
  {
    testMinMax(6, 233);
    testMinMax(-7, 50);
    testMinMax(-7, -3);
    testMinMax(0, 0);
  }

  @Test
  public void failsToTranslateWithMinGreaterThanMax()
  {
    String valueStr = translateTo("!randomNumber number1 (30 1)");
    assertThat(valueStr, is("!randomNumber number1 (30 1)"));
  }

  @Test
  public void failsToTranslateWithoutVariable()
  {
    String valueStr = translateTo("!randomNumber (30 1)");
    assertThat(valueStr, is("!randomNumber (30 1)"));
  }

  private void testMinMax(int min, int max)
  {
    int length = max - min + 1;
    int counter = 0;
    Set<Integer> allNumbers = new HashSet<Integer>();

    while (allNumbers.size() != length)
    {
      validateMaxIterations(counter++);

      String valueStr = translateTo("!randomNumber number1 (" + min + " " + max + ")");
      int value = Integer.parseInt(extractValue(valueStr));

      allNumbers.add(value);

      assertThat(value, is(both(greaterThanOrEqualTo(min)).and(lessThanOrEqualTo(max))));
    }
  }

  private static String translateTo(final String input)
  {
    final TestSourcePage page = new TestSourcePage();
    return ParserTestHelper.translateTo(page, input);
  }

  private static void validateMaxIterations(int counter)
  {
    if (counter >= MAX_ITERATIONS)
    {
      throw new RuntimeException("counter reached max iterations - check your code for appropriate min/max value");
    }
  }

  private static String extractValue(String str)
  {
    String _res = str.replace("<span class=\"meta\">random number defined: number1=", "");
    _res = _res.replace("</span>", "");
    return _res.trim();
  }

}
