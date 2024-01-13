package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.RawHtml;

import java.util.Collection;
import java.util.Collections;

public class Collapsible extends SymbolType implements Rule, Translation {

    public static final String CLOSED = " closed";
    private static final String INVISIBLE = " invisible";

    public Collapsible() {
      super("Collapsible");
      wikiMatcher(new Matcher().startLine().string("!").repeat('*').optional(">", "<").whitespace());
      wikiRule(this);
      htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol titleText = parser.parseToIgnoreFirst(SymbolType.Newline);
        if (parser.atEnd()) return Symbol.nothing;

        Symbol bodyText = parser.parseTo(SymbolType.CloseCollapsible);
        if (parser.atEnd()) {
            return new Maybe<>(Symbol.listOf(
              current.asText(), titleText, new Symbol(SymbolType.Newline), bodyText));
        }

        // Remove trailing newline so we do not introduce excessive whitespace in the page.
        if (parser.peek().isType(SymbolType.Newline)) {
            parser.moveNext(1);
        }

        return new Maybe<>(current
          .add(titleText)
          .add(bodyText));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        String option = symbol.hasProperty(">") ? CLOSED : symbol.hasProperty("<") ? INVISIBLE : "";
        String title = translator.translate(symbol.childAt(0));
        String body = translator.translate(symbol.childAt(1));
        return generateHtml(option, title, body);
    }

    public static String generateHtml(String state, String titleText, String bodyText) {
        return generateHtml(state, titleText, bodyText, Collections.emptySet());
    }

    public static String generateHtml(String state, String titleText, String bodyText, Collection<String> extraClasses) {
        StringBuilder outerClasses = new StringBuilder("collapsible" + state);
        for (String extraClass : extraClasses) {
            outerClasses.append(' ').append(extraClass);
        }

        HtmlTag outerBlock = new HtmlTag("div");
        outerBlock.addAttribute("class", outerClasses.toString());

        outerBlock.add(new RawHtml("<ul>" +
        		"<li><a href='#' class='expandall'>Expand</a></li>" +
        		"<li><a href='#' class='collapseall'>Collapse</a></li>" +
        		"</ul>"));

      	HtmlTag title = new HtmlTag("p", titleText);
        title.addAttribute("class", "title");
        outerBlock.add(title);

        HtmlTag body = new HtmlTag("div", bodyText);
        outerBlock.add(body);

        return outerBlock.html();
    }
}
