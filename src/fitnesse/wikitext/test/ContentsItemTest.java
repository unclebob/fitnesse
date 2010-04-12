package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.translator.ContentsItemBuilder;
import fitnesse.wikitext.translator.VariableSource;
import fitnesse.wikitext.widgets.TOCWidget;
import org.junit.Test;
import util.Maybe;

import static org.junit.Assert.assertEquals;

public class ContentsItemTest {
    @Test
    public void buildsPlainItem() throws Exception {
        Symbol contents = new Symbol(SymbolType.Contents);
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, new TestVariableSource("blah", "blah"));
        assertEquals("<a href=\"PlainItem\">PlainItem</a>" + HtmlElement.endl,
                builder.buildItem(new TestRoot().makePage("PlainItem")).html());
    }

    @Test
    public void buildsFilterItemFromOption() throws Exception {
        Symbol contents = new Symbol(SymbolType.Contents);
        contents.add(new Symbol(SymbolType.Text, "-f"));
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, new TestVariableSource("blah", "blah"));
        assertEquals("<a href=\"PlainItem\">PlainItem (F1)</a>" + HtmlElement.endl,
                builder.buildItem(withProperties(new TestRoot().makePage("PlainItem"), new String[]{"Suites=F1"})).html());
    }

    @Test
    public void buildsFilterItemFromVariable() throws Exception {
        Symbol contents = new Symbol(SymbolType.Contents);
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, new TestVariableSource(TOCWidget.FILTER_TOC, "true"));
        assertEquals("<a href=\"PlainItem\">PlainItem (F1)</a>" + HtmlElement.endl,
                builder.buildItem(withProperties(new TestRoot().makePage("PlainItem"), new String[]{"Suites=F1"})).html());
    }

    @Test
    public void buildsWithHelpTitle() throws Exception {
        Symbol contents = new Symbol(SymbolType.Contents);
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, new TestVariableSource("blah", "blah"));
        assertEquals("<a href=\"PlainItem\" title=\"help\">PlainItem</a>" + HtmlElement.endl,
                builder.buildItem(withProperties(new TestRoot().makePage("PlainItem"), new String[]{"Help=help"})).html());
    }

    @Test
    public void buildsHelpItemFromOption() throws Exception {
        Symbol contents = new Symbol(SymbolType.Contents);
        contents.add(new Symbol(SymbolType.Text, "-h"));
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, new TestVariableSource("blah", "blah"));
        assertEquals("<a href=\"PlainItem\">PlainItem</a><span class=\"pageHelp\">: help</span>" + HtmlElement.endl,
                builder.buildItem(withProperties(new TestRoot().makePage("PlainItem"), new String[]{"Help=help"})).html());
    }

    @Test
    public void buildsHelpItemFromVariable() throws Exception {
        Symbol contents = new Symbol(SymbolType.Contents);
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, new TestVariableSource(TOCWidget.HELP_TOC, "true"));
        assertEquals("<a href=\"PlainItem\">PlainItem</a><span class=\"pageHelp\">: help</span>" + HtmlElement.endl,
                builder.buildItem(withProperties(new TestRoot().makePage("PlainItem"), new String[]{"Help=help"})).html());
    }

    private WikiPage withProperties(WikiPage page, String[] propList) throws Exception {
        PageData data = page.getData();
        WikiPageProperties props = data.getProperties();
        for (int i = 0; i < propList.length; i++) {
            String[] parts = propList[i].split("=");
            if (parts.length == 1) props.set(parts[0]);
            else props.set(parts[0], parts[1]);
          }

        page.commit(data);
        return page;
    }

    private class TestVariableSource implements VariableSource {
        private String name;
        private String value;

        public TestVariableSource(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Maybe<String> findVariable(String requestedName) {
            return requestedName.equals(name) ? new Maybe<String>(value) : Maybe.noString;
        }
    }
}
