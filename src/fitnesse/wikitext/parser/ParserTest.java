package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {
    public static  void assertScans(String input, String expected) {
        Scanner scanner = new Scanner(input);
        assertScans(expected, scanner);
    }

    public static  void assertScansTokenType(String input, TokenType expected, boolean found) {
        Scanner scanner = new Scanner(input);
        while (true) {
            scanner.moveNext();
            if (scanner.isEnd()) break;
            if (scanner.isType(expected)) {
                assertTrue(found);
                return;
            }
        }
        assertTrue(!found);
    }

    public static void assertScans(String expected, Scanner scanner) {
        StringBuilder result = new StringBuilder();
        while (true) {
            scanner.moveNext();
            if (scanner.isEnd()) break;
            if (result.length() > 0) result.append(",");
            Token current = scanner.getCurrent();
            String name = current.getType().toString();
            result.append(name);
            String content = current.getContent();
            if (content.length() > 0) result.append("=").append(content);
        }
        assertEquals(expected, result.toString());
    }

    public static void assertTranslates(String input, String expected) {
        assertTranslates(null, input, expected);
    }

    public static void assertTranslates(WikiPage page, String input, String expected) {
        Translator translator = new Translator(page);
        assertEquals(expected, translator.translate(input));
    }
}
