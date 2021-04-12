package fitnesse.wikitext.parser.decorator;

import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.Table;
import org.junit.Test;

import static fitnesse.wikitext.parser.decorator.SymbolInspector.inspect;
import static java.util.Arrays.stream;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SymbolInspectorTest {

  private Symbol table = new Symbol(Table.symbolType);

  @Test(expected = NullPointerException.class)
  public void should_fail_to_create_inspector_when_symbol_is_null() {
    inspect(null);
  }

  @Test
  public void should_pass_symbol_type_check() {
    inspect(table).checkSymbolType(Table.symbolType);
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_symbol_type_check_when_mismatch() {
    inspect(table).checkSymbolType(Table.tableCell);
  }

  @Test
  public void should_get_raw_content() {
    Symbol symbolWithContent = symbol(symbol(symbol("a"),
                                             symbol(""),
                                             symbol("b")),
                                      symbol(" "),
                                      symbol("c"));
    assertThat(inspect(symbolWithContent).getRawContent(), is("ab c"));
  }


  private Symbol symbol(Symbol... children) {
    Symbol symbol = new Symbol(Table.tableCell);
    stream(children).forEach(symbol::add);
    return symbol;
  }

  private Symbol symbol(String contents) {
    return new Symbol(Table.tableCell, contents);
  }
}
