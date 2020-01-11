package fitnesse.wikitext.parser.decorator;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.Table;
import fitnesse.wikitext.parser.VariableSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SlimTableDefaultColoringTest {

  private Symbol         table = spy(new Symbol(Table.symbolType));
  @Mock
  private VariableSource variableSource;

  private SlimTableDefaultColoring slimTableDefaultColoring = new SlimTableDefaultColoring();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void should_leave_table_contents_alone_when_not_slim_test_system() {
    givenTestSystem("fit");
    slimTableDefaultColoring.handleParsedSymbol(table, variableSource);
    verify(table, never()).getChildren();
  }

  @Test
  public void should_decorate_table_contents_by_default_when_slim_test_system() {
    givenTestSystem("slim");
    slimTableDefaultColoring.handleParsedSymbol(table, variableSource);
    verify(table, atLeastOnce()).getChildren();
  }

  @Test
  public void should_leave_table_contents_alone_when_disabled() {
    givenTestSystem("slim");
    SlimTableDefaultColoring.disableForTable(table);
    slimTableDefaultColoring.handleParsedSymbol(table, variableSource);
    verify(table, never()).getChildren();
  }

  private void givenTestSystem(String testSystem) {
    when(variableSource.findVariable("TEST_SYSTEM")).thenReturn(new Maybe<>(testSystem));
  }
}
