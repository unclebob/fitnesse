package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import util.Maybe;

public class Collapsible extends SymbolType implements Rule, Translation {

    private static final String STATE = "State";
    public static final String CLOSED = " closed";
    private static final String INVISIBLE = " invisible";

    public Collapsible() {
      super("Collapsible");
      wikiMatcher(new Matcher().startLine().string("!").repeat('*'));
      wikiRule(this);
      htmlTranslation(this);
  }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        String state = "";
        Symbol next = parser.moveNext(1);
        if (next.getContent().equals(">")) {
            state = CLOSED;
            next = parser.moveNext(1);
        }
        else if (next.getContent().equals("<")) {
            state = INVISIBLE;
            next = parser.moveNext(1);
        }
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        Symbol titleText = parser.parseToIgnoreFirst(SymbolType.Newline);
        if (parser.atEnd()) return Symbol.nothing;

        Symbol bodyText = parser.parseTo(SymbolType.CloseCollapsible);
        if (parser.atEnd()) return Symbol.nothing;

        return new Maybe<Symbol>(current
                .putProperty(STATE, state)
                .add(titleText)
                .add(bodyText));
    }

    public String toTarget(Translator translator, Symbol symbol) {
        String option = symbol.getProperty(Collapsible.STATE);
        String title = translator.translate(symbol.childAt(0));
        String body = translator.translate(symbol.childAt(1));
        return generateHtml(option, title, body);
    }

    private String makeInvisibleSection(String body) {
        HtmlTag section = new HtmlTag("div", body);
        section.addAttribute("class", "invisible");
        return section.html();
    }

    public static String generateHtml(String state, String titleText, String bodyText) {
        HtmlTag outerBlock = new HtmlTag("div");
        outerBlock.addAttribute("class", "collapsible" + state);
        
        outerBlock.add(new RawHtml("<ul>" +
        		"<li><a href='#' class='expandall'>Expand All</a></li>" +
        		"<li><a href='#' class='collapseall'>Collapse All</a></li>" +
        		"</ul>"));

      	HtmlTag title = new HtmlTag("p", titleText);
        title.addAttribute("class", "title");
        outerBlock.add(title);
        
        HtmlTag body = new HtmlTag("div", bodyText);
        outerBlock.add(body);
        
        return outerBlock.html();
    }
    

}
