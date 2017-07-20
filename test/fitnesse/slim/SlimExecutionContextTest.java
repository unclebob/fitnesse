package fitnesse.slim;

import org.junit.Test;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SlimExecutionContextTest {

  @Test
  public void shouldAddLibrary() {
    Library library = new Library("library", new Object());

    SlimExecutionContext context = new SlimExecutionContext(new DefaultInteraction());
    context.addLibrary(library);

    assertEquals(1, context.getLibraries().size());
    assertEquals(library, context.getLibraries().get(0));
  }

  @Test
  public void shouldReplaceSymbols() {
    SlimExecutionContext context = new SlimExecutionContext(new DefaultInteraction());
    context.setVariable("first", "var1");
    context.setVariable("second", "var2");

    assertArrayEquals(new Object[] {"test", "var1", "foo", "bar", "var2"}, context.replaceSymbols(new Object[] {
        "test", "$first", "foo", "bar", "$second"}));
  }
}
