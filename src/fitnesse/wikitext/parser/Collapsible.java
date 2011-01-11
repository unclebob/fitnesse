package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import util.Maybe;

public class Collapsible extends SymbolType implements Rule, Translation {

    public Collapsible() {
        super("Collapsible");
        wikiMatcher(new Matcher().startLine().string("!").repeat('*'));
        wikiRule(this);
        htmlTranslation(this);
    }

    private static final String State = "State";
    public static final String Open = "Open";
    public static final String Closed = "Closed";
    private static final String Invisible = "Invisible";

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String state = Open;
        Symbol next = parser.moveNext(1);
        if (next.getContent().equals(">")) {
            state = Closed;
            next = parser.moveNext(1);
        }
        else if (next.getContent().equals("<")) {
            state = Invisible;
            next = parser.moveNext(1);
        }
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        Symbol titleText = parser.parseToIgnoreFirst(SymbolType.Newline);
        if (parser.atEnd()) return Symbol.nothing;

        Symbol bodyText = parser.parseTo(SymbolType.CloseCollapsible);
        if (parser.atEnd()) return Symbol.nothing;

        return new Maybe<Symbol>(current
                .putProperty(State, state)
                .add(titleText)
                .add(bodyText));
    }
    private static long nextId = 1;

    public static void resetId() { nextId = 1; }

    public String toTarget(Translator translator, Symbol symbol) {
        String option = symbol.getProperty(Collapsible.State);
        String title = translator.translate(symbol.childAt(0));
        String body = translator.translate(symbol.childAt(1));
        return option.equals(Collapsible.Invisible)
                ? makeInvisibleSection(body)
                : generateHtml(option, title, body);
    }

    private String makeInvisibleSection(String body) {
        HtmlTag section = new HtmlTag("div", body);
        section.addAttribute("class", "invisible");
        return section.html();
    }

    public static String generateHtml(String state, String titleText, String bodyText) {
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
        image.addAttribute("src", "/files/images/collapsable" + state + ".gif");
        image.addAttribute("class", "left");
        image.addAttribute("id", "img" + Long.toString(id));
        toggle.add(image);
        outerBlock.add(toggle);
        outerBlock.add("&nbsp;");
        HtmlTag title = HtmlUtil.makeSpanTag("meta", titleText);
        outerBlock.add(title);
        HtmlTag body = new HtmlTag("div", bodyText);
        body.addAttribute("class", bodyClass(state));
        body.addAttribute("id", Long.toString(id));
        outerBlock.add(body);
        return outerBlock.html();
    }

    private static String bodyClass(String state) {
        return state.equals(Collapsible.Open) ? "collapsable"
               : state.equals(Collapsible.Closed) ? "hidden"
               : "invisible";
    }
}
