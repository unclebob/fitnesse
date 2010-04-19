package fitnesse.wikitext.test;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;
import fitnesse.wikitext.translator.Translator;
import fitnesse.wikitext.parser.VariableSource;

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
            Symbol current = scanner.getCurrent();
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

    public static void assertTranslatesTo(String input, VariableSource variableSource, String expected) {
        assertEquals(expected, translateToHtml(null, input, variableSource));
    }

    public static void assertTranslatesTo(WikiPage page, String input, String expected) {
        assertEquals(expected, translateTo(page, input));
    }

    public static void assertTranslatesTo(WikiPage page, String expected) throws Exception {
        assertEquals(expected, translateTo(page));
    }

    public static String translateTo(WikiPage page, String input) {
        return translateToHtml(page, input);
    }

    public static String translateToHtml(WikiPage page, String input) {
        Symbol list = Parser.make(new ParsingPage(page), input).parse();
        return new Translator(page, list).translate();
    }

    public static String translateToHtml(WikiPage page, String input, VariableSource variableSource) {
        Symbol list = Parser.make(new ParsingPage(page), input, variableSource).parse();
        return new Translator(page, list, variableSource).translate();
    }

    public static String translateTo(WikiPage page) throws Exception {
        return page.getData().getHtml();
    }

    public static void assertParses(String input, String expected) throws Exception {
        WikiPage page = new TestRoot().makePage("TestPage", input);
        Symbol result = parse(page, input);
        assertEquals(expected, serialize(result));
    }

    public static Symbol parse(WikiPage page) throws Exception {
        return Parser.make(new ParsingPage(page), page.getData().getContent()).parse();
    }

    private static Symbol parse(WikiPage page, String input) {
        return Parser.make(new ParsingPage(page), input).parse();
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
