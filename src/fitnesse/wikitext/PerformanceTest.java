package fitnesse.wikitext;

import fitnesse.wikitext.parser.TestRoot;
import fitnesse.wikitext.parser.Translator;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PerformanceTest {
    private String pageContent = "";
    public PerformanceTest() {
        for (int i = 0; i < 5000; i++) {
            pageContent += "|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|\n";
        }
    }

    @Ignore @Test
    public void OldParser() throws Exception {
        long start = System.currentTimeMillis();
        ParentWidget root = new WidgetRoot(pageContent, new TestRoot().makePage("OldTest"),
            WidgetBuilder.htmlWidgetBuilder);
        String result = root.render();
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(result);
        assertEquals("done", "done");
    }

    @Ignore @Test
    public void NewParser() throws Exception {
        long start = System.currentTimeMillis();
        String result = new Translator(new TestRoot().makePage("NewTest")).translate(pageContent);
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(result);
        assertEquals("done", "done");
    }
}
