package fitnesse.wikitext.parser;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ScannerTest {
    @Test public void copyRestoresState() {
        Scanner scanner = new Scanner(new TestSourcePage(), "stuff");
        Scanner backup = new Scanner(scanner);
        ParserTestHelper.assertScans("Text=stuff", scanner);
        ParserTestHelper.assertScans("", scanner);
        scanner.copy(backup);
        ParserTestHelper.assertScans("Text=stuff", scanner);
    }

    @Test public void terminatedLiteralAddsTerminator() {
        Scanner scanner = new Scanner(new TestSourcePage(), "stuff\n");
        scanner.makeLiteral(SymbolType.Newline);
        assertTrue(scanner.getCurrent().isType(SymbolType.Newline));
    }

    @Test public void unterminatedLiteralAddsEmpty() {
        Scanner scanner = new Scanner(new TestSourcePage(), "stuff");
        scanner.makeLiteral(SymbolType.Newline);
        assertTrue(scanner.getCurrent().isType(SymbolType.Empty));
    }
}
