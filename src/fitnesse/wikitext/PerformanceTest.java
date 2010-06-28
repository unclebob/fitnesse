package fitnesse.wikitext;

import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.test.TestRoot;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PerformanceTest {
    private String tablePageContent = "";
    private String definePageContent = "";
    public PerformanceTest() {
        for (int i = 0; i < 500; i++) {
            tablePageContent += "|aaaaaaaaaa|bbbbbbbbbb|cccccccccc|dddddddddd|eeeeeeeeee|ffffffffff|gggggggggg|hhhhhhhhhh|iiiiiiiiiii|jjjjjjjjjj|kkkkkkkkkk|lllllllllll|mmmmmmmmmm|nnnnnnnnnn|oooooooooo|pppppppppp|qqqqqqqqqq|rrrrrrrrrr|ssssssssss|tttttttttt|uuuuuuuuuu|vvvvvvvvvv|wwwwwwwwww|xxxxxxxxxx|yyyyyyyyyy|zzzzzzzzzz|\n";
            definePageContent += "!define variable" + i + " {aaaaaaaaaa bbbbbbbbbb cccccccccc dddddddddd eeeeeeeeee ffffffffff gggggggggg hhhhhhhhhh iiiiiiiiii jjjjjjjjjj kkkkkkkkkk llllllllll mmmmmmmmmm nnnnnnnnnn oooooooooo pppppppppp qqqqqqqqqq rrrrrrrrrr ssssssssss tttttttttt uuuuuuuuuu vvvvvvvvvv wwwwwwwwww xxxxxxxxxx yyyyyyyyyy zzzzzzzzzz}\n";
        }
    }

    @Test
    public void OldParserTable() throws Exception {
        runOldParser(tablePageContent);
    }

    @Test
    public void OldParserDefine() throws Exception {
    //    runOldParser(definePageContent);
    }

    private void runOldParser(String pageContent) throws Exception {
        long start = System.currentTimeMillis();
        ParentWidget root = new WidgetRoot(pageContent, new TestRoot().makePage("OldTest"),
            WidgetBuilder.htmlWidgetBuilder);
        String result = root.render();
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(result);
        assertEquals("done", "done");
    }

    @Test
    public void NewParserTable() throws Exception {
        runNewParser(tablePageContent);
    }

    @Test
    public void NewParserDefine() throws Exception {
        runNewParser(definePageContent);
    }

    private void runNewParser(String pageContent) throws Exception {
        long start = System.currentTimeMillis();
        String result = ParserTest.translateTo(new TestRoot().makePage("NewTest"), pageContent);
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(result);
        assertEquals("done", "done");
    }
}
