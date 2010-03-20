package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import util.Maybe;

public class CollapsibleToken extends Token {
    private static long nextId = 1;

    public static void resetId() { nextId = 1; }

    public CollapsibleToken(String content) { super(content); }

    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Whitespace) return Maybe.noString;
        
        String titleText = new Translator(getPage()).translate(scanner, TokenType.Newline);
        if (scanner.isEnd()) return Maybe.noString;
        
        String bodyText = new Translator(getPage()).translate(scanner, TokenType.EndSection);
        if (scanner.isEnd()) return Maybe.noString;

        return new Maybe<String>(generateHtml(titleText, bodyText));
    }

    public String generateHtml(String titleText, String bodyText) {
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
        body.addAttribute("class", getContent());
        body.addAttribute("id", Long.toString(id));
        outerBlock.add(body);
        return outerBlock.html();
    }

    public TokenType getType() { return TokenType.Collapsible; }
}
