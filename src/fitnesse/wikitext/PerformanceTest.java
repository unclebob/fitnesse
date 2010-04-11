package fitnesse.wikitext;

import fitnesse.wikitext.test.TestRoot;
import fitnesse.wikitext.translator.Translator;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PerformanceTest {
    private String pageContent = "";
    public PerformanceTest() {
        for (int i = 0; i < 500; i++) {
            pageContent += "|a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|\n";
        }
    }

    @Test
    public void OldParser() throws Exception {
        long start = System.currentTimeMillis();
        ParentWidget root = new WidgetRoot(pageContent, new TestRoot().makePage("OldTest"),
            WidgetBuilder.htmlWidgetBuilder);
        String result = root.render();
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(result);
        assertEquals("done", "done");
    }

    @Test
    public void NewParser() throws Exception {
        long start = System.currentTimeMillis();
        String result = Translator.translateToHtml(new TestRoot().makePage("NewTest"), pageContent);
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(result);
        assertEquals("done", "done");
    }
}
