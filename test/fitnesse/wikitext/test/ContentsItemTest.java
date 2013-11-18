package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContentsItemTest {
    @Test
    public void buildsPlain() throws Exception {
        assertBuilds("PlainItem", new String[] {}, "", "", "<a href=\"PlainItem\" class=\"static\">PlainItem</a>");
    }

    @Test
    public void buildsWithHelp() throws Exception {
        assertBuilds("PlainItem", new String[]{"Help=help"}, "", "", "<a href=\"PlainItem\" class=\"static\" title=\"help\">PlainItem</a>");
    }

    @Test
    public void buildsFilter() throws Exception {
        assertBuildsOption("PlainItem", new String[]{"Suites=F1"}, "-f", "FILTER_TOC", "<a href=\"PlainItem\" class=\"static\">PlainItem (F1)</a>");
    }

    @Test
    public void buildsHelp() throws Exception {
        assertBuildsOption("PlainItem", new String[]{"Help=help"}, "-h", "HELP_TOC", "<a href=\"PlainItem\" class=\"static\">PlainItem</a><span class=\"pageHelp\">: help</span>");
    }

    @Test
    public void buildsPropertiesSuite() throws Exception {
        assertBuildsOption("PlainItem", new String[]{"Suite=true", "WikiImport=true", "Prune=true"}, "-p", "PROPERTY_TOC",
                "<a href=\"PlainItem\" class=\"suite linked pruned\">PlainItem *@-</a>");
    }

    @Test
    public void buildsPropertiesTest() throws Exception {
        assertBuildsOption("PlainItem", new String[]{"Test=true", "WikiImport=true", "Prune=true"}, "-p", "PROPERTY_TOC",
                "<a href=\"PlainItem\" class=\"test linked pruned\">PlainItem +@-</a>");
    }

    @Test
    public void buildsRegraced() throws Exception {
        assertBuildsOption("PlainItem", new String[]{}, "-g", "REGRACE_TOC", "<a href=\"PlainItem\" class=\"static\">Plain Item</a>");
    }

    private void assertBuildsOption(String page, String[] properties, String option, String variable, String result) throws Exception {
        assertBuilds(page, properties, option, "", result);
        assertBuilds(page, properties, "", variable, result);
    }

    private void assertBuilds(String page, String[] properties, String option, String variable, String result) throws Exception {
        Symbol contents = new Symbol(new Contents());
        contents.add(new Symbol(SymbolType.Text, option));
        contents.evaluateVariables(new String[] {variable},new TestVariableSource(variable, "true"));
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, 1);
        assertEquals(result + HtmlElement.endl, builder.buildItem(new WikiSourcePage(withProperties(new TestRoot().makePage(page), properties))).html());
    }

    private WikiPage withProperties(WikiPage page, String[] propList) throws Exception {
        PageData data = page.getData();
        WikiPageProperties props = data.getProperties();
        for (String aPropList : propList) {
            String[] parts = aPropList.split("=");
            if (parts.length == 1) props.set(parts[0]);
            else props.set(parts[0], parts[1]);
        }

        page.commit(data);
        return page;
    }

}
