package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.parser.HtmlWriter;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HtmlWriterTest {
    @Test public void writesSimpleTag() {
        HtmlWriter writer = new HtmlWriter();
        writer.putTag("simple");
        assertEquals("<simple />" + HtmlElement.endl, writer.toHtml());
    }

    @Test public void startsAndEndsSimpleTag() {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("simple");
        writer.endTag();
        assertEquals("<simple />" + HtmlElement.endl, writer.toHtml());
    }

    @Test public void writesSimpleTagWithAttribute() {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("simple");
        writer.putAttribute("name", "value");
        writer.endTag();
        assertEquals("<simple name=\"value\" />" + HtmlElement.endl, writer.toHtml());
    }
    @Test public void writesSimpleTagWithText() {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("simple");
        writer.putText("stuff");
        writer.endTag();
        assertEquals("<simple>stuff</simple>" + HtmlElement.endl, writer.toHtml());
    }

    @Test public void writesNestedTag() {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("parent");
        writer.putTag("child");
        writer.endTag();
        assertEquals("<parent>" + HtmlElement.endl + "\t<child />" + HtmlElement.endl + "</parent>" + HtmlElement.endl, writer.toHtml());
    }

    @Test public void writesMultipleNestedTags() {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("parent");
        writer.putTag("child");
        writer.putTag("child");
        writer.endTag();
        assertEquals("<parent>" + HtmlElement.endl + "\t<child />" + HtmlElement.endl + "\t<child />" + HtmlElement.endl + "</parent>" + HtmlElement.endl, writer.toHtml());
    }

    @Test public void writesNestedTagInline() {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("parent");
        writer.putTagInline("child");
        writer.endTag();
        assertEquals("<parent><child /></parent>" + HtmlElement.endl, writer.toHtml());
    }

    @Test public void writesNestedTagWithAttribute() {
        HtmlWriter writer = new HtmlWriter();
        writer.startTag("parent");
        writer.putAttribute("name", "value");
        writer.putTag("child");
        writer.endTag();
        assertEquals("<parent name=\"value\">" + HtmlElement.endl + "\t<child />" + HtmlElement.endl + "</parent>" + HtmlElement.endl, writer.toHtml());
    }

}
