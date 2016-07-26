package fitnesse.wikitext.parser;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;

import java.util.Collection;
import java.util.Collections;

public class Include extends SymbolType implements Rule, Translation {
    private static final String[] setUpSymbols = new String[] {"COLLAPSE_SETUP"};
    private static final String includeHelpOption = "-h";
    public static final String TEARDOWN = "teardown";

    public Include() {
        super("Include");
        wikiMatcher(new Matcher().startLineOrCell().string("!include"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol next = parser.moveNext(1);
        if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;

        next = parser.moveNext(1);
        String option = "";
        if ((next.isType(SymbolType.Text) && next.getContent().startsWith("-")) || next.isType(SymbolType.DateFormatOption)) {
            option = next.getContent() + (next.isType(SymbolType.DateFormatOption) ? parser.moveNext(1).getContent() : "");
            next = parser.moveNext(1);
            if (!next.isType(SymbolType.Whitespace)) return Symbol.nothing;
            next = parser.moveNext(1);
        }
        current.add(option);
        if (!next.isType(SymbolType.Text) && !next.isType(WikiWord.symbolType)) return Symbol.nothing;

        String includedPageName = next.getContent();
        while (parser.peek().isType(SymbolType.Text) || parser.peek().isType(WikiWord.symbolType)) {
          Symbol remainderOfPageName = parser.moveNext(1);
          includedPageName += remainderOfPageName.getContent();
        }

        SourcePage sourcePage = parser.getPage().getNamedPage();

        // Record the page name anyway, since we might want to show an error if it's invalid
        if (PathParser.isWikiPath(includedPageName)) {
          current.add(new Symbol(new WikiWord(sourcePage), includedPageName));
        } else {
          current.add(includedPageName);
        }

        Maybe<SourcePage> includedPage = sourcePage.findIncludedPage(includedPageName);
        if (includedPage.isNothing()) {
          current.add("").add(new Symbol(SymbolType.Style, "error").add(includedPage.because()));
        }
        else if (includeHelpOption.equals(option)) {
        	String helpText = includedPage.getValue().getProperty(PageData.PropertyHELP);
        	current.add("").add(Parser.make(
        			parser.getPage(),helpText).parse());
        } else {
            current.childAt(1).putProperty(WikiWord.WITH_EDIT, "true");
            ParsingPage included = option.equals("-setup") || option.equals("-teardown")
                    ? parser.getPage()
                    : parser.getPage().copyForNamedPage(includedPage.getValue());
            current.add("").add(Parser.make(
                            included,
                            includedPage.getValue().getContent())
                            .parse());
            if (option.equals("-setup")) current.evaluateVariables(setUpSymbols, parser.getVariableSource());
        }

      // Remove trailing newline so we do not introduce excessive whitespace in the page.
      if (parser.peek().isType(SymbolType.Newline)) {
        parser.moveNext(1);
      }

      return new Maybe<>(current);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        if (symbol.getChildren().size() < 4) {
            return translator.translate(symbol.childAt(2));
        }
        String option = symbol.childAt(0).getContent();
        if (option.equals("-seamless")) {
            return translator.translate(symbol.childAt(3));
        } else if (includeHelpOption.equals(option)) {
        	return translator.translate(symbol.childAt(3));
        } else {
            String collapseState = stateForOption(option, symbol);
            String title = "Included page: "
                    + translator.translate(symbol.childAt(1));
            Collection<String> extraCollapsibleClass =
                    option.equals("-teardown") ? Collections.singleton(TEARDOWN) : Collections.<String>emptySet();
            return Collapsible.generateHtml(collapseState, title, translator.translate(symbol.childAt(3)), extraCollapsibleClass);
        }
    }

    private String stateForOption(String option, Symbol symbol) {
        return ((option.equals("-setup") || option.equals("-teardown")) && symbol.getVariable("COLLAPSE_SETUP", "true").equals("true"))
                || option.equals("-c")
                ? Collapsible.CLOSED
                : "";
    }
}
