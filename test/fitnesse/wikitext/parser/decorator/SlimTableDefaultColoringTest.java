package fitnesse.wikitext.parser.decorator;

import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.SourcePage;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.Table;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SlimTableDefaultColoringTest {

  private Symbol table = spy(new Symbol(Table.symbolType));
  @Mock
  private ParsingPage variableSource;
  @Mock
  private SourcePage sourcePage;

  private SlimTableDefaultColoring slimTableDefaultColoring = new SlimTableDefaultColoring(new SlimTableFactory());

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    when(variableSource.getPage()).thenReturn(sourcePage);
  }

  @Test
  public void should_leave_table_contents_alone_when_not_slim_test_system() {
    givenTestSystem("fit");
    slimTableDefaultColoring.handleParsedSymbol(table, variableSource);
    verify(table, never()).getChildren();
    verify(sourcePage, never()).hasProperty(any());
  }

  @Test
  public void should_decorate_table_contents_when_slim_test_system_test_page() {
    givenTestSystem("slim");
    when(sourcePage.hasProperty("Test")).thenReturn(true);
    slimTableDefaultColoring.handleParsedSymbol(table, variableSource);
    verify(table).getChildren();
  }

  @Test
  public void should_not_decorate_table_contents_by_default_when_slim_test_system_not_test_page() {
    givenTestSystem("slim");
    when(sourcePage.hasProperty("Test")).thenReturn(false);
    slimTableDefaultColoring.handleParsedSymbol(table, variableSource);
    verify(table, never()).getChildren();
  }

  private void givenTestSystem(String testSystem) {
    when(variableSource.findVariable("TEST_SYSTEM")).thenReturn(new Maybe<>(testSystem));
  }
}
