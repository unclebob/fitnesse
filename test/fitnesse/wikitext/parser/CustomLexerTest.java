package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * This is an example of how a custom lexer can be used in conjunction with the wiki parser.
 *
 * The interface is modeled after the IDEA Lexer class.
 */
public class CustomLexerTest {

  @Test
  public void testLexer() {
    String buffer = "This ''is'': a WikiWord";

    assertEquals(asList("Text:This", "Whitespace: ", "Italic:''is''", "Colon::", "Whitespace: ", "Text:a", "Whitespace: ", "WikiWord:WikiWord"),
            lex(buffer));
  }

  @Test
  public void shouldIdentifyVariables() {
    String buffer = "A ${VARIABLE} for the win";

    assertEquals(asList("Text:A", "Whitespace: ", "Variable:${VARIABLE}", "Whitespace: ", "Text:for", "Whitespace: ", "Text:the", "Whitespace: ", "Text:win"),
            lex(buffer));
  }

  @Test
  public void shouldInclude() {
    String buffer = "!include -seamless .WikiWord";

    assertEquals(asList("Include:!include -seamless .WikiWord"),
            lex(buffer));
  }

  @Test
  public void shouldTraverseCollapsedSections() {
    String buffer = "!** what about\n" +
            "me\n" +
            "*!";

    assertEquals(asList("Collapsible:!** what about\n" +
                            "me\n" +
                            "*!",
                    "SymbolList:what about\n", "Text:what", "Whitespace: ", "Text:about",
                    "SymbolList:me\n*!", "Text:me", "Newline:\n"),
            lex(buffer));
  }

  @Test
  public void shouldTraverseTables() {
    String buffer = "|script: table|\n|ensure|I'm there|\n";

    assertEquals(asList("Table:|script: table|\n" +
                            "|ensure|I'm there|\n",
                    "TableRow:script: table|\n|", "TableCell:script: table|\n|", "Text:script", "Colon::", "Whitespace: ", "Text:table",
                    "TableRow:ensure|I'm there|\n", "TableCell:ensure|", "Text:ensure", "TableCell:I'm there|\n", "Text:I'm", "Whitespace: ", "Text:there"),
            lex(buffer));
  }

  public List<String> lex(CharSequence buffer) {
    Lexer lexer = new Lexer(buffer);
    List<String> lexedTokens = new ArrayList<>();
    // lexer.start
    while (lexer.getTokenType() != null) {
      TokenType tokenType = lexer.getTokenType();
      String tokenText = buffer.subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
      lexedTokens.add(tokenType.toString() + ":" + tokenText);
      lexer.advance();
    }
    return lexedTokens;
  }

  private static class Lexer {

    private final ParseSpecification specification;
    private final Scanner scanner;
    private final Parser parser;

    private Iterator<Symbol> symbolIterator = emptyIterator();
    private Symbol currentSymbol;

    public Lexer(CharSequence buffer) {
      this(buffer, 0, buffer.length());
    }

    public Lexer(CharSequence buffer, int startOffset, int endOffset) {
      Parser.make(new LexerParsingPage(), buffer.subSequence(startOffset, endOffset)).parse();

      ParsingPage currentPage = new LexerParsingPage();
      CharSequence input = buffer.subSequence(startOffset, endOffset);

      specification = new ParseSpecification().provider(SymbolProvider.wikiParsingProvider);
      scanner = new Scanner(new TextMaker(currentPage, currentPage.getNamedPage()), input);
      parser = new Parser(null, currentPage, scanner, specification);

      advance();
    }

    /**
     * Returns the token at the current position of the lexer or <code>null</code> if lexing is finished.
     *
     * @return the current token.
     */
    public TokenType getTokenType() {
      return currentSymbol != null ? new TokenType(currentSymbol.getType()) : null;
    }

    /**
     * Returns the start offset of the current token.
     *
     * @return the current token start offset.
     */
    public int getTokenStart() {
      return currentSymbol.getStartOffset();
    }

    /**
     * Returns the end offset of the current token.
     *
     * @return the current token end offset.
     */

    public int getTokenEnd() {
      return currentSymbol.getEndOffset();
    }

    /**
     * Advances the lexer to the next token.
     */
    public void advance() {
      if (symbolIterator.hasNext()) {
        currentSymbol = symbolIterator.next();
        if (!currentSymbol.hasOffset()) advance();
      } else {
        Maybe<Symbol> parsedSymbol = specification.parseSymbol(parser, scanner);
        if (parsedSymbol.isNothing()) {
          currentSymbol = null;
        } else {
          currentSymbol = parsedSymbol.getValue();
          if (shouldTraverse(currentSymbol)) {
            symbolIterator = new SymbolChildIterator(currentSymbol.getChildren());
          } else {
            symbolIterator = emptyIterator();
          }
        }
      }
    }

    private boolean shouldTraverse(Symbol symbol) {
      return "Table".equals(symbol.getType().toString()) || "Collapsible".equals(symbol.getType().toString());
    }

  }

  public static class TokenType {
    private final SymbolType type;

    public TokenType(SymbolType type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return type.toString();
    }
  }

  public static class SymbolChildIterator implements Iterator<Symbol> {
    private final Iterator<Symbol> symbols;
    private Iterator<Symbol> childIterator = emptyIterator();

    SymbolChildIterator(Collection<Symbol> symbols) {
      this.symbols = symbols.iterator();
    }

    @Override
    public boolean hasNext() {
      return symbols.hasNext() || childIterator.hasNext();
    }

    @Override
    public Symbol next() {
      if (!childIterator.hasNext()) {
        // initial call:
        Symbol next = symbols.next();
        childIterator = new SymbolChildIterator(next.getChildren());
        return next;
      }
      return childIterator.next();
    }

    @Override
    public void remove() {
      throw new IllegalStateException("Can not remove symbols from the tree");
    }
  }

  public static class LexerParsingPage extends ParsingPage {
    public LexerParsingPage() {
      super(new LexerSourcePage());
    }

    @Override
    public ParsingPage copyForNamedPage(SourcePage namedPage) {
      // Used in Include
      throw new IllegalStateException("Should not have been called in this context");
    }

    @Override
    public void putVariable(String name, String value) {
      super.putVariable(name, value);
    }

    @Override
    public Maybe<String> findVariable(String name) {
      return super.findVariable(name);
    }
  }

  public static class LexerSourcePage implements SourcePage {

    @Override
    public String getName() {
      // Used in Contents and WikiWord
      return null;
    }

    @Override
    public String getFullName() {
      // Used in Contents
      return null;
    }

    @Override
    public String getPath() {
      // Used in ParsingPage
      throw new IllegalStateException("Should not have been called in this context");
    }

    @Override
    public String getFullPath() {
      // Used in Help -- isn't getPath enough?
      throw new IllegalStateException("Should not have been called in this context");
    }

    @Override
    public String getContent() {
      // Used in Include
      throw new IllegalStateException("Should not have been called in this context");
    }

    @Override
    public boolean targetExists(String wikiWordPath) {
      return false;
    }

    @Override
    public String makeFullPathOfTarget(String wikiWordPath) {
      // Used in WikiWord
      return null;
    }

    @Override
    public String findParentPath(String targetName) {
      // Used in WikiWord
      return null;
    }

    @Override
    public Maybe<SourcePage> findIncludedPage(String pageName) {
      // Used in Include
      return Maybe.nothingBecause("not in this context");
    }

    @Override
    public Collection<SourcePage> getChildren() {
      // Used in Contents
      return Collections.emptyList();
    }

    @Override
    public boolean hasProperty(String propertyKey) {
      return false;
    }

    @Override
    public String getProperty(String propertyKey) {
      throw new IllegalStateException("Should not have been called in this context");
    }

    @Override
    public String makeUrl(String wikiWordPath) {
      throw new IllegalStateException("Should not have been called in this context");
    }

    @Override
    public int compareTo(SourcePage o) {
      throw new IllegalStateException("Should not have been called in this context");
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> emptyIterator() {
    return (Iterator<T>) EmptyIterator.EMPTY_ITERATOR;
  }

  private static class EmptyIterator<E> implements Iterator<E> {
    static final EmptyIterator<Object> EMPTY_ITERATOR
            = new EmptyIterator<>();

    @Override
    public boolean hasNext() { return false; }
    @Override
    public E next() { throw new NoSuchElementException(); }
    @Override
    public void remove() { throw new IllegalStateException(); }
  }

}
