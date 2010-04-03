package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;
import fitnesse.wikitext.translator.Translator;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {
    public static  void assertScans(String input, String expected) {
        Scanner scanner = new Scanner(input);
        assertScans(expected, scanner);
    }

    public static  void assertScansTokenType(String input, SymbolType expected, boolean found) {
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

    public static void assertTranslatesTo(String input, String expected) {
        assertTranslatesTo(null, input, expected);
    }

    public static void assertTranslatesTo(WikiPage page, String input, String expected) {
        assertEquals(expected, translateTo(page, input));
    }

    public static String translateTo(WikiPage page, String input) {
        return new Translator(page).translateToHtml(input);
    }

    public static String translateTo(WikiPage page) throws Exception {
        return translateTo(page, page.getData().getContent());
    }

    public static void assertParses(String input, String expected) {
        Symbol result = new Parser(null).parse(input);
        assertEquals(expected, serialize(result));
    }

    public static String serialize(Symbol symbol) {
        StringBuilder result = new StringBuilder();
        result.append(symbol.getType() != null ? symbol.getType().toString() : "?no type?");
        int i = 0;
        for (Symbol child : symbol.getChildren()) {
            result.append(i == 0 ? "[" : ", ");
            result.append(serialize(child));
            i++;
        }
        if (i > 0) result.append("]");
        return result.toString();
    }
}
