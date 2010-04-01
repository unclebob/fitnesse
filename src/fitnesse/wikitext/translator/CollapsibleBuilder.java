package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.parser.Symbol;

public class CollapsibleBuilder implements Translation {
    private static long nextId = 1;

    public static void resetId() { nextId = 1; }

    public HtmlTag toHtml(Translator translator, Symbol symbol) {
        long id = nextId++;
        HtmlTag outerBlock = new HtmlTag("div");
        outerBlock.addAttribute("class", "collapse_rim");
        HtmlTag floatRight = new HtmlTag("div");
        floatRight.addAttribute("style", "float: right;");
        floatRight.addAttribute("class", "meta");
        HtmlTag expand = new HtmlTag("a", "Expand All");
        expand.addAttribute("href", "javascript:expandAll();");
        floatRight.add(expand);
        floatRight.add(" | ");
        HtmlTag collapse = new HtmlTag("a", "Collapse All");
        collapse.addAttribute("href", "javascript:collapseAll();");
        floatRight.add(collapse);
        outerBlock.add(floatRight);
        HtmlTag toggle = new HtmlTag("a");
        toggle.addAttribute("href", "javascript:toggleCollapsable('" + Long.toString(id) + "');");
        HtmlTag image = new HtmlTag("img");
        image.addAttribute("src", "/files/images/collapsableOpen.gif");
        image.addAttribute("class", "left");
        image.addAttribute("id", "img" + Long.toString(id));
        toggle.add(image);
        outerBlock.add(toggle);
        outerBlock.add("&nbsp;");
        HtmlTag title = HtmlUtil.makeSpanTag("meta", translator.translate(symbol.childAt(1)));
        outerBlock.add(title);
        HtmlTag body = new HtmlTag("div", translator.translate(symbol.childAt(2)));
        body.addAttribute("class", translator.translate(symbol.childAt(0)));
        body.addAttribute("id", Long.toString(id));
        outerBlock.add(body);
        return outerBlock;
    }
}
