package fitnesse.wikitext.parser;

import fitnesse.wikitext.shared.ContentsItemBuilder;
import org.junit.Test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertBuildsOption("PlainItem", new String[]{"Help=help"}, "-h", "HELP_TOC", "<a href=\"PlainItem\" class=\"static\">PlainItem</a>" + HtmlElement.endl + "\t<span class=\"pageHelp\">: help</span>");
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

    @Test
    public void buildsTestPageCountForASuitePageWithNoChildren() throws Exception {
      assertBuildsOption("PlainItem", new String[]{"Suite=true"}, "-c", "TEST_PAGE_COUNT_TOC",
        "<a href=\"PlainItem\" class=\"suite\">PlainItem ( 0 )</a>");
    }

    @Test
    public void buildsTestPageCountForASuiteWithTestPage() throws Exception {
      TestRoot root = new TestRoot();
      WikiPage pageOne = root.makePage("PageOne", "!contents -R1 -g -p -f -h -c");
      setPageProperties(pageOne,"Suite");
      WikiPage pageTwo = pageOne.addChildPage("PageTwo");
      setPageProperties(pageTwo,"Suite");
      setPageProperties(pageTwo.addChildPage("TestOne"),"Test");
      //SuiteSetUp and SuiteTearDown should be counted as test just like they are counted when run
      setPageProperties(pageTwo.addChildPage("SuiteSetUp"),"Static");
      setPageProperties(pageTwo.addChildPage("SuiteTearDown"),"Static");
      assertTrue(pageOne.getHtml().contains("Page Two (  3 ) *"));
    }

    @Test
    public void buildsTestPageCountForASuiteWithSetUpAndTearDown() throws Exception {
      TestRoot root = new TestRoot();
      WikiPage pageOne = root.makePage("PageOne", "!contents -R1 -g -p -f -h -c");
      setPageProperties(pageOne,"Suite");
      WikiPage pageTwo = pageOne.addChildPage("PageTwo");
      setPageProperties(pageTwo,"Suite");
      setPageProperties(pageTwo.addChildPage("SampleTest"),"Test");
      //SetUp and TearDown should not be counted as tests.
      setPageProperties(pageTwo.addChildPage("SetUp"),"Static");
      setPageProperties(pageTwo.addChildPage("TearDown"),"Static");
      assertTrue(pageOne.getHtml().contains("Page Two (  1 ) *"));
    }

    @Test
    public void buildsTestPageCountForATest() throws Exception {
      assertBuildsOption("PlainItem", new String[]{"Test=true"}, "-c", "TEST_PAGE_COUNT_TOC",
        "<a href=\"PlainItem\" class=\"test\">PlainItem</a>");
    }

    @Test
    public void assertBuildsSymbolicLinkSuffix() throws Exception{
        Symbol contents = new Symbol(new Contents());
        contents.putProperty("-p", "");

        WikiPage root = InMemoryPage.makeRoot("RooT");
        WikiPage pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "page one");
        WikiPage pageOneChild = WikiPageUtil.addPage(pageOne, PathParser.parse("PageOne.PageOneChild"), "page one child");

        // Make Symbolic Link at root that links to PageOne.PageOneChild.
        SymbolicPage symPage = new SymbolicPage("SymPage", pageOneChild, root);
        PageData data = root.getData();
        data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymPage", "PageOne.PageOneChild");
        root.commit(data);

        WikiSourcePage rootPage = new WikiSourcePage(root);
        WikiSourcePage symWikiPage = new WikiSourcePage(symPage);

        ContentsItemBuilder builder = new ContentsItemBuilder(contents, 1, rootPage);
        assertEquals("<li>" + HtmlElement.endl + "\t" + "<a href=\"SymPage\" class=\"static\">SymPage ></a>"
                + HtmlElement.endl + "</li>" + HtmlElement.endl, builder.buildItem(symWikiPage).html());
    }

    private void assertBuildsOption(String page, String[] properties, String option, String variable, String result) throws Exception {
        assertBuilds(page, properties, option, "", result);
        assertBuilds(page, properties, "", variable, result);
    }

    private void assertBuilds(String page, String[] properties, String option, String variable, String result) throws Exception {
        Symbol contents = new Symbol(new Contents());
        contents.putProperty(option, "");
        contents.copyVariables(new String[] {variable},new TestVariableSource(variable, "true"));
        ContentsItemBuilder builder = new ContentsItemBuilder(contents, 1);
        assertEquals("<li>" + HtmlElement.endl + "\t" + result + HtmlElement.endl + "</li>" + HtmlElement.endl,
                builder.buildItem(new WikiSourcePage(withProperties(new TestRoot().makePage(page), properties))).html());
    }

    private WikiPage withProperties(WikiPage page, String[] propList) throws Exception {
        PageData data = page.getData();
        WikiPageProperty props = data.getProperties();
        for (String aPropList : propList) {
            String[] parts = aPropList.split("=");
            if (parts.length == 1) props.set(parts[0]);
            else props.set(parts[0], parts[1]);
        }

        page.commit(data);
        return page;
    }

    private void setPageProperties(WikiPage pageOne, String properties){
      PageData pageOneData = pageOne.getData();
      pageOneData.setAttribute(properties);
      pageOne.commit(pageOneData);
    }

}
