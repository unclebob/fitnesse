package fitnesse.wikitext.parser;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ScannerTest {
    @Test public void scansTextAsWords() {
        ParserTest.assertScans("hi mom", "Word=hi,Whitespace= ,Word=mom");
    }

    @Test public void copyRestoresState() {
        Scanner scanner = new Scanner("stuff");
        Scanner backup = new Scanner(scanner);
        ParserTest.assertScans("Word=stuff", scanner);
        ParserTest.assertScans("", scanner);
        scanner.copy(backup);
        ParserTest.assertScans("Word=stuff", scanner);
    }
}
