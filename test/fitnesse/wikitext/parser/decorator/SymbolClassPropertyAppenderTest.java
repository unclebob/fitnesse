package fitnesse.wikitext.parser.decorator;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

import static fitnesse.wikitext.parser.decorator.SymbolClassPropertyAppender.CLASS_PROPERTY_NAME;
import static fitnesse.wikitext.parser.decorator.SymbolClassPropertyAppender.classPropertyAppender;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SymbolClassPropertyAppenderTest {

  private Symbol testSymbol = new Symbol(new SymbolType("TestSymbol"));

  private SymbolClassPropertyAppender appender = classPropertyAppender();


  @Test
  public void should_have_no_class_prop_by_default() {
    assertThat(testSymbol.hasProperty(CLASS_PROPERTY_NAME), is(false));
    assertThat(testSymbol.getProperty(CLASS_PROPERTY_NAME), is(""));
  }

  @Test
  public void should_have_class_prop_when_appended() {
    appender.addPropertyValue(testSymbol, "abc");
    assertThat(testSymbol.hasProperty(CLASS_PROPERTY_NAME), is(true));
    assertThat(testSymbol.getProperty(CLASS_PROPERTY_NAME), is("abc"));
  }

  @Test
  public void should_have_concatenated_values_when_multiple_appended() {
    appender.addPropertyValue(testSymbol, "abc");
    appender.addPropertyValue(testSymbol, "def");
    appender.addPropertyValue(testSymbol, "ghi");
    assertThat(testSymbol.getProperty(CLASS_PROPERTY_NAME), is("abc def ghi"));
  }

  @Test
  public void should_not_append_same_value_twice() {
    appender.addPropertyValue(testSymbol, "abc");
    appender.addPropertyValue(testSymbol, "def");
    appender.addPropertyValue(testSymbol, "abc");
    assertThat(testSymbol.getProperty(CLASS_PROPERTY_NAME), is("abc def"));
  }

  @Test
  public void should_append_similiar_values() {
    appender.addPropertyValue(testSymbol, "abc");
    appender.addPropertyValue(testSymbol, "ab");
    appender.addPropertyValue(testSymbol, "c");
    assertThat(testSymbol.getProperty(CLASS_PROPERTY_NAME), is("abc ab c"));
  }
}
