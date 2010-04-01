package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import util.Maybe;

public class CollapsibleRule extends Rule {
    private static long nextId = 1;

    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        String bodyClass = "collapsable";
        scanner.moveNext();
        if (scanner.getCurrentContent().equals(">")) {
            bodyClass = "hidden";
            scanner.moveNext();
        }
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;

        Phrase titleText = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;

        Phrase bodyText = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.CloseCollapsible);
        if (scanner.isEnd()) return Symbol.Nothing;

        return new Maybe<Symbol>(new Phrase(SymbolType.Collapsible)
                .add(bodyClass)
                .add(titleText)
                .add(bodyText));
    }

    public String generateHtml(String titleText, String bodyText, String bodyClass) {
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
        HtmlTag title = HtmlUtil.makeSpanTag("meta", titleText);
        outerBlock.add(title);
        HtmlTag body = new HtmlTag("div", bodyText);
        body.addAttribute("class", bodyClass);
        body.addAttribute("id", Long.toString(id));
        outerBlock.add(body);
        return outerBlock.html();
    }

    public SymbolType getType() { return SymbolType.Collapsible; }
}
