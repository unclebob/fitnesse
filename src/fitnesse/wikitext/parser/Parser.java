package fitnesse.wikitext.parser;

import util.Maybe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.arraycopy;

public class Parser {
    public static final HashMap<SymbolType, Rule> rules = new HashMap<SymbolType, Rule>();
    static {
        addRule(SymbolType.Alias, new AliasRule());
        addRule(SymbolType.AnchorName, new AnchorNameRule());
        addRule(SymbolType.AnchorReference, new AnchorReferenceRule());
        addRule(SymbolType.Bold, new EqualPairRule());
        addRule(SymbolType.CenterLine, new LineRule());
        addRule(SymbolType.Collapsible, new CollapsibleRule());
        addRule(SymbolType.Comment, new CommentRule());
        addRule(SymbolType.Contents, new ContentsRule());
        addRule(SymbolType.Define, new DefineRule());
        addRule(SymbolType.Evaluator, new EvaluatorRule());
        addRule(SymbolType.HashTable, new HashTableRule());
        addRule(SymbolType.HeaderLine, new LineRule());
        addRule(SymbolType.Image, new ImageRule());
        addRule(SymbolType.Include, new IncludeRule());
        addRule(SymbolType.Italic, new EqualPairRule());
        addRule(SymbolType.Link, new LinkRule());
        addRule(SymbolType.Literal, new LiteralRule());
        addRule(SymbolType.Meta, new LineRule());
        addRule(SymbolType.NoteLine, new LineRule());
        addRule(SymbolType.OrderedList, new ListRule());
        addRule(SymbolType.Path, new PathRule());
        addRule(SymbolType.PlainTextTable, new PlainTextTableRule());
        addRule(SymbolType.Preformat, new LiteralRule());
        addRule(SymbolType.See, new SeeRule());
        addRule(SymbolType.Strike, new EqualPairRule());
        addRule(SymbolType.Style, new StyleRule());
        addRule(SymbolType.Table, new TableRule());
        addRule(SymbolType.Today, new TodayRule());
        addRule(SymbolType.UnorderedList, new ListRule());
        addRule(SymbolType.Variable, new VariableRule());
    }
    private static final SymbolType[] emptyTypes = new SymbolType[] {};
    private static final ArrayList<Symbol> emptySymbols = new ArrayList<Symbol>();
    
    private static void addRule(SymbolType symbolType, Rule rule) {
        rules.put(symbolType, rule);
    }

    public static Parser make(ParsingPage currentPage, String input) {
        return make(currentPage, input, SymbolProvider.wikiParsingProvider);
    }

    public static Parser make(ParsingPage currentPage, String input, SymbolProvider provider) {
        return make(currentPage, input, new VariableFinder(currentPage), provider);
    }

    public static Parser make(ParsingPage currentPage, String input, VariableSource variableSource, SymbolProvider provider) {
        return new Parser(currentPage, new Scanner(new TextMaker(variableSource), input), provider, variableSource, emptyTypes, emptyTypes, emptyTypes);
    }

    private ParsingPage currentPage;
    private SymbolProvider provider;
    private VariableSource variableSource;
    private Scanner scanner;
    private SymbolType[] terminators;
    private SymbolType[] ignoresFirst;
    private SymbolType[] ends;

    public Parser(ParsingPage currentPage, Scanner scanner, SymbolProvider provider, VariableSource variableSource, SymbolType[] terminators, SymbolType[] ignoresFirst, SymbolType[] ends) {
        this.currentPage = currentPage;
        this.scanner = scanner;
        this.provider = provider;
        this.terminators = terminators;
        this.ignoresFirst = ignoresFirst;
        this.ends = ends;
        this.variableSource = variableSource;
    }

    public ParsingPage getPage() { return currentPage; }
    public VariableSource getVariableSource() { return variableSource; }

    public Symbol getCurrent() { return scanner.getCurrent(); }
    public boolean atEnd() { return scanner.isEnd(); }
    public boolean atLast() { return scanner.isLast(); }
    public boolean isMoveNext(SymbolType type) { return moveNext(1).isType(type); }
    
    public Symbol moveNext(int count) {
        for (int i = 0; i < count; i++) scanner.moveNext();
        return scanner.getCurrent();
    }

    public List<Symbol> moveNext(SymbolType[] symbolTypes) {
        ArrayList<Symbol> tokens = new ArrayList<Symbol>();
        for (SymbolType type: symbolTypes) {
            Symbol current = moveNext(1);
            if (!current.isType(type)) return new ArrayList<Symbol>();
            tokens.add(current);
        }
        return tokens;
    }

    public List<Symbol> peek(SymbolType[] types) {
        List<Symbol> lookAhead = scanner.peek(types.length, provider, new ArrayList<SymbolType>());
        if (lookAhead.size() != types.length) return emptySymbols;
        for (int i = 0; i < lookAhead.size(); i++) {
            if (!lookAhead.get(i).isType(types[i])) return emptySymbols;
        }
        return lookAhead;
    }

    public String parseToAsString(SymbolType terminator) {
        int start = scanner.getOffset();
        scanner.markStart();
        parseTo(terminator);
        return scanner.substring(start, scanner.getOffset() - 1);
    }

    public String parseLiteral(SymbolType terminator) {
        scanner.makeLiteral(terminator);
        String literal = scanner.getCurrent().getContent();
        scanner.moveNext();
        return literal;
    }

    public Symbol parse(String input) {
        return new Parser(currentPage, new Scanner(new TextMaker(variableSource), input), provider, variableSource, emptyTypes, emptyTypes, emptyTypes).parse();
    }

    public Symbol parseToIgnoreFirst(SymbolType type) {
        return parseToIgnoreFirst(new SymbolType[] {type});
    }

    public Symbol parseToIgnoreFirst(SymbolType[] types) {
        return new Parser(currentPage, scanner, provider, variableSource, types, types, emptyTypes).parse();
    }

    public Symbol parseToIgnoreFirstWithSymbols(SymbolType ignore, SymbolProvider provider) {
        SymbolType[] ignores = new SymbolType[] {ignore};
        return new Parser(currentPage, scanner, provider, variableSource, ignores, ignores, emptyTypes).parse();
    }
    
    public Symbol parseTo(SymbolType terminator) {
        return parseTo(new SymbolType[] {terminator});
    }

    public Symbol parseTo(SymbolType[] terminators) {
        return new Parser(currentPage, scanner, SymbolProvider.wikiParsingProvider, variableSource, terminators, emptyTypes, emptyTypes).parse();
    }

    public Symbol parseToWithSymbols(SymbolType terminator, SymbolProvider provider) {
        SymbolType[] terminators = new SymbolType[] {terminator};
        return parseToWithSymbols(terminators, provider);
    }

    public Symbol parseToWithSymbols(SymbolType[] terminators, SymbolProvider provider) {
        return new Parser(currentPage, scanner, provider, variableSource, terminators, emptyTypes, emptyTypes).parse();
    }

    public Symbol parseWithEnds(SymbolType[] ends) {
        return new Parser(currentPage, scanner, SymbolProvider.wikiParsingProvider, variableSource, emptyTypes, emptyTypes, makeEndList(ends)).parse();
    }

    public Symbol parseWithEnds(SymbolProvider provider, SymbolType[] types) {
        provider.addTypes(types);
        return new Parser(currentPage, scanner, provider, variableSource, emptyTypes, emptyTypes, makeEndList(types)).parse();
    }

    public Symbol parse() {
        Symbol result = new Symbol(SymbolType.SymbolList);
        ArrayList<SymbolType> ignore = new ArrayList<SymbolType>();
        ignore.addAll(Arrays.asList(ignoresFirst));
        while (true) {
            Scanner backup = new Scanner(scanner);
            scanner.moveNextIgnoreFirst(provider, ignore);
            if (scanner.isEnd()) break;
            Symbol currentToken = scanner.getCurrent();
            if (contains(ends, currentToken.getType())) {
                scanner.copy(backup);
                break;
            }
            if (contains(terminators, currentToken.getType())) break;
            if (rules.containsKey(currentToken.getType())) {
                Maybe<Symbol> parsedSymbol = rules.get(currentToken.getType()).parse(currentToken, this);
                if (parsedSymbol.isNothing()) {
                    ignore.add(currentToken.getType());
                    scanner.copy(backup);
                }
                else {
                    result.add(parsedSymbol.getValue());
                    ignore.clear();
                }
            }
            else {
                result.add(currentToken);
                ignore.clear();
            }
        }
        return result;
    }

    private boolean contains(SymbolType[] terminators, SymbolType currentType) {
        for (SymbolType terminator: terminators)
            if (currentType == terminator) return true;
        return false;
    }

    private SymbolType[] makeEndList(SymbolType[] terminators) {
        SymbolType[] parentEnds = this.ends;
        SymbolType[] parentTerminators = this.terminators;
        SymbolType[] ends = new SymbolType[parentTerminators.length + parentEnds.length + terminators.length];
        arraycopy(parentEnds, 0, ends, 0, parentEnds.length);
        arraycopy(parentTerminators, 0, ends, parentEnds.length, parentTerminators.length);
        arraycopy(terminators, 0, ends, parentTerminators.length + parentEnds.length, terminators.length);
        return ends;
    }
}
