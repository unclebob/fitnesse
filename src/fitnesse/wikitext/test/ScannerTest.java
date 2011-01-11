package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.Scanner;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ScannerTest {
    @Test public void copyRestoresState() {
        Scanner scanner = new Scanner(new TestSourcePage(), "stuff");
        Scanner backup = new Scanner(scanner);
        ParserTestHelper.assertScans("Text=stuff", scanner);
        ParserTestHelper.assertScans("", scanner);
        scanner.copy(backup);
        ParserTestHelper.assertScans("Text=stuff", scanner);
    }
}
