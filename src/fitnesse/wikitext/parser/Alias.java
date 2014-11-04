package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.Utils;
import util.Maybe;

public class Alias extends SymbolType implements Rule, Translation {
    public static final Alias symbolType = new Alias();

    public Alias() {
        super("Alias");
        wikiMatcher(new Matcher().string("[["));
        wikiRule(this);
        htmlTranslation(this);
    }

    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol tag = parser.parseToIgnoreFirst(SymbolType.CloseBracket);
        if (!parser.isMoveNext(SymbolType.OpenBracket)) return Symbol.nothing;

        Symbol link = parser.parseToIgnoreFirstWithSymbols(SymbolType.CloseBracket, SymbolProvider.aliasLinkProvider);
        if (!parser.isMoveNext(SymbolType.CloseBracket)) return Symbol.nothing;

        return new Maybe<Symbol>(current.add(tag).add(link));
    }

    public String toTarget(Translator translator, Symbol symbol) {
        if (symbol.childAt(0).childAt(0).isType(WikiWord.symbolType)) return translator.translate(symbol.childAt(0));

        String linkBody = translator.translate(symbol.childAt(0));
        String linkReferenceString = Utils.unescapeHTML(translator.translate(symbol.childAt(1)));
        ParsingPage parsingPage = ((HtmlTranslator)translator).getParsingPage();
        Symbol linkReference = Parser.make(parsingPage, linkReferenceString).parseToIgnoreFirst(Comment.symbolType);

        if (linkReference.childAt(0).isType(WikiWord.symbolType)) {
            return new WikiWordBuilder(translator.getPage(), linkReference.childAt(0).getContent(), linkBody)
                    .buildLink(translator.translate(linkReference.childrenAfter(0)), linkBody);
        }

        if (linkReference.childAt(0).isType(Link.symbolType)) {
            return Link.symbolType.buildLink(translator, linkBody, linkReference.childAt(0));
        }

        HtmlTag alias = new HtmlTag("a", linkBody);
        alias.addAttribute("href", translator.translate(linkReference));
        return alias.htmlInline();
    }
}
