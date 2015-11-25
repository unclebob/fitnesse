package fitnesse.wikitext.parser;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RandomStringTest
{
  private static int MAX_ITERATIONS = 1000; // Higher value = higher accuracy

  @Test
  public void translatesRandomStrings()
  {
    executeValidation("3 6 a-z,3-8,$", "[a-z3-8$]{3,6}");
    executeValidation("6 6 c-e", "[c-e]{6}");
    executeValidation("6 6 AB", "[A-B]{6}");
  }

  @Test
  public void testRandomness()
  {
    Set<String> allStrings = new HashSet<String>();
    int combinations = 340; // (4*1) + (4*4) + (4*16) + (4*64) + (4*64) = 340
    int maxIterations = 100000; // Sanity check
    int counter = 0;

    while (allStrings.size() != combinations)
    {
      if (counter++ > maxIterations)
      {
        fail("Failed to find all combinations");
      }

      String valueStr = extractValue(translateTo("!randomString string1 (1 4 az$0)"));
      allStrings.add(valueStr);
    }
  }

  @Test
  public void failsToTranslateWithMaxGreaterThanMin()
  {
    String valueStr = extractValue(translateTo("!randomString string1 (7 6 AB)"));
    assertThat(valueStr, is("!randomString string1 (7 6 AB)"));
  }

  private void executeValidation(String randomStringConfig, String regex)
  {
    for (int i = 0; i < MAX_ITERATIONS; i++)
    {
      String valueStr = extractValue(translateTo("!randomString string1 (" + randomStringConfig + ")"));
      assertThat(valueStr, valueStr.matches(regex), is(true));
    }
  }

  private static String translateTo(final String input)
  {
    final TestSourcePage page = new TestSourcePage();
    return ParserTestHelper.translateTo(page, input);
  }

  private static String extractValue(String str)
  {
    String _res = str.replace("<span class=\"meta\">random string defined: string1=", "");
    _res = _res.replace("</span>", "");
    return _res.trim();
  }
}
