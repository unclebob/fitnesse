package fitnesse.wikitext;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;
import fitnesse.wikitext.test.TestRoot;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PerformanceTest {
    private String tablePageContent = "";
    private String definePageContent = "";
    public PerformanceTest() {
        StringBuilder table = new StringBuilder();
        StringBuilder define = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            table.append("|aaaaaaaaaa|bbbbbbbbbb|cccccccccc|dddddddddd|eeeeeeeeee|ffffffffff|gggggggggg|hhhhhhhhhh|iiiiiiiiiii|jjjjjjjjjj|kkkkkkkkkk|lllllllllll|mmmmmmmmmm|nnnnnnnnnn|oooooooooo|pppppppppp|qqqqqqqqqq|rrrrrrrrrr|ssssssssss|tttttttttt|uuuuuuuuuu|vvvvvvvvvv|wwwwwwwwww|xxxxxxxxxx|yyyyyyyyyy|zzzzzzzzzz|\n");
            define.append("!define variable").append(i).append(" {aaaaaaaaaa bbbbbbbbbb cccccccccc dddddddddd eeeeeeeeee ffffffffff gggggggggg hhhhhhhhhh iiiiiiiiii jjjjjjjjjj kkkkkkkkkk llllllllll mmmmmmmmmm nnnnnnnnnn oooooooooo pppppppppp qqqqqqqqqq rrrrrrrrrr ssssssssss tttttttttt uuuuuuuuuu vvvvvvvvvv wwwwwwwwww xxxxxxxxxx yyyyyyyyyy zzzzzzzzzz}\n");
        }
        tablePageContent = table.toString();
        definePageContent = define.toString();
    }

    @Test
    public void NewParserTable() throws Exception {
        runNewParser(tablePageContent);
    }

    @Test
    public void NewParserDefine() throws Exception {
        runNewParser(definePageContent);
    }

    private void runNewParser(String input) throws Exception {
        long start = System.currentTimeMillis();
        WikiPage page = new TestRoot().makePage("NewTest");
        //String result = ParserTest.translateTo(new TestRoot().makePage("NewTest"), pageContent);
        Symbol list = Parser.make(new ParsingPage(new WikiSourcePage(page)), input).parse();
        System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        /*String result =*/ new HtmlTranslator(new WikiSourcePage(page), new ParsingPage(new WikiSourcePage(page))).translateTree(list);
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(result);
        assertEquals("done", "done");
    }
}
