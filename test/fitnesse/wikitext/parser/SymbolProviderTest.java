package fitnesse.wikitext.parser;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class SymbolProviderTest {
  @Test
  public void findsMatch() {
    SymbolProvider testProvider = new SymbolProvider(new SymbolType[] {SymbolType.OpenBrace});
    assertMatches(testProvider, testMatch, '{');
  }

  @Test
  public void findsNoMatch() {
    SymbolProvider testProvider = new SymbolProvider(new SymbolType[] {SymbolType.OpenBrace});
    assertMatches(testProvider, SymbolMatch.noMatch, '}');
   }

  @Test
  public void findsMatchFromParent() {
    SymbolProvider parentProvider = new SymbolProvider(new SymbolType[] {SymbolType.CloseBrace});
    SymbolProvider testProvider = new SymbolProvider(parentProvider);
    parentProvider.add(SymbolType.OpenBrace);
    assertMatches(testProvider, testMatch, '{');
   }

   @Test
   public void copiedProviderMatchesOriginal() {
     SymbolProvider testProvider = new SymbolProvider(new SymbolType[] {SymbolType.OpenBrace});
     SymbolProvider copiedProvider = SymbolProvider.copy(testProvider);
     assertMatches(copiedProvider, testMatch, '{');
   }

  @Test
  public void symbolTypeCanBeRemovedFromProvider() {
    SymbolProvider testProvider = new SymbolProvider(new SymbolType[] {SymbolType.OpenBrace});
    testProvider.remove(SymbolType.OpenBrace);
    assertMatches(testProvider, SymbolMatch.noMatch, '{');
  }

  private void assertMatches(SymbolProvider testProvider, final SymbolMatch expected, char startCharacter) {
    SymbolMatch result = testProvider.findMatch(startCharacter, new SymbolMatcher() {
      @Override
      public SymbolMatch makeMatch(Matchable candidate) {
        return candidate.matchesFor(SymbolType.OpenBrace)
                ? testMatch
                : SymbolMatch.noMatch;
      }
    });
    assertSame(expected, result);
  }

  private final SymbolMatch testMatch = new SymbolMatch(SymbolType.OpenBrace, "hi", 0);
}
