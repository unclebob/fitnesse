package fitnesse.wikitext.parser;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    public static  void assertScans(String input, String expected) {
        Scanner scanner = new Scanner(input);
        assertScans(expected, scanner);
    }

    public static void assertScans(String expected, Scanner scanner) {
        StringBuilder result = new StringBuilder();
        while (true) {
            scanner.moveNext();
            if (scanner.isEnd()) break;
            if (result.length() > 0) result.append(",");
            Token current = scanner.getCurrent();
            String name = current.getClass().getSimpleName();
            result.append(name);
            String string = current.toString();
            if (!string.contains(name)) result.append("=").append(string);
        }
        assertEquals(expected, result.toString());
    }

    public static void assertTranslates(String input, String expected) {
        Translator translator = new Translator();
        assertEquals(expected, translator.translate(input));
    }
}
