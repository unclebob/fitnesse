package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wikitext.ParsingPage;

public class Alias extends SymbolType implements Rule, Translation {
    public static final Alias symbolType = new Alias();

    public Alias() {
        super("Alias");
        wikiMatcher(new Matcher().string("[["));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol tag = parser.parseToIgnoreFirst(SymbolType.CloseBracket);
        if (!parser.isMoveNext(SymbolType.OpenBracket)) return Symbol.nothing;

        Symbol link = parser.parseToIgnoreFirstWithSymbols(SymbolType.CloseBracket, SymbolProvider.aliasLinkProvider);
        if (!parser.isMoveNext(SymbolType.CloseBracket)) return Symbol.nothing;

        return new Maybe<>(current.add(tag).add(link));
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        if (symbol.childAt(0).childAt(0).isType(WikiWord.symbolType)) return translator.translate(symbol.childAt(0));

        String linkBody = translator.translate(symbol.childAt(0));
        String linkReferenceString = HtmlUtil.unescapeHTML(translator.translate(symbol.childAt(1)));
        ParsingPage parsingPage = ((HtmlTranslator)translator).getParsingPage();
        Symbol linkReference = Parser.make(parsingPage, linkReferenceString).parseToIgnoreFirst(Comment.symbolType);

        if (linkReference.childAt(0).isType(WikiWord.symbolType) || (linkReference.getChildren().size() == 1 && PathParser.isWikiPath(linkReference.childAt(0).getContent()))) {
            return new WikiWordBuilder(translator.getPage(), linkReference.childAt(0).getContent(), linkBody)
                    .buildLink(translator.translate(linkReference.childrenAfter(0)), linkBody);
        }

        HtmlTag alias = new HtmlTag("a", linkBody);

        if (linkReference.childAt(0).isType(Link.symbolType)) {
            alias.addAttribute("href", linkReferenceString.startsWith("http://files/") ? linkReferenceString.substring(7) : linkReferenceString);
        } else {
            alias.addAttribute("href", translator.translate(linkReference));
        }

        return alias.htmlInline();
    }
}
