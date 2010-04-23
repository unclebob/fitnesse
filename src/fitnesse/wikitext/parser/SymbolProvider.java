package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class SymbolProvider {
    /* Keeping these tables in sync with the matchers is a hassle but a major performance gain */
    /* We could build these tables automatically... */

    private static char defaultMatch = '\0';
    private static HashMap<Character, Matchable[]> dispatch;
    static {
        dispatch = new HashMap<Character, Matchable[]>();
        for (char letter = 'a'; letter <= 'z'; letter++) dispatch.put(letter, new SymbolType[] {});
        for (char letter = 'A'; letter <= 'Z'; letter++) dispatch.put(letter, new SymbolType[] {});
        for (char digit = '0'; digit <= '9'; digit++) dispatch.put(digit, new SymbolType[] {});

        dispatch.put('h', new SymbolType[] { SymbolType.Link });
        dispatch.put('|', new SymbolType[] { SymbolType.Table, SymbolType.EndCell });
        dispatch.put('!', new SymbolType[] {
                SymbolType.HashTable, SymbolType.HeaderLine, SymbolType.Literal, SymbolType.Collapsible,
                SymbolType.AnchorName, SymbolType.Contents, SymbolType.CenterLine, SymbolType.Define,
                SymbolType.Include, SymbolType.Meta, SymbolType.NoteLine, SymbolType.Path, SymbolType.PlainTextTable,
                SymbolType.See, SymbolType.Style, SymbolType.Table });
        dispatch.put('-', new SymbolType[] {
                SymbolType.HorizontalRule, SymbolType.Table, SymbolType.CloseLiteral, SymbolType.Strike});

        /* This is the default list. These can be broken out further */
        dispatch.put(defaultMatch, new SymbolType[] {
            SymbolType.Alias, SymbolType.UnorderedList, SymbolType.OrderedList, SymbolType.Comment, SymbolType.Whitespace, SymbolType.CloseCollapsible,
            SymbolType.Newline, SymbolType.Colon, SymbolType.Comma,
            SymbolType.Evaluator, SymbolType.CloseEvaluator, SymbolType.Variable, SymbolType.Preformat,
            SymbolType.ClosePreformat, SymbolType.OpenParenthesis, SymbolType.OpenBrace, SymbolType.OpenBracket,
            SymbolType.CloseParenthesis, SymbolType.CloseBrace, SymbolType.ClosePlainTextTable, SymbolType.CloseBracket, SymbolType.CloseLiteral,
            SymbolType.Collapsible, SymbolType.HorizontalRule, SymbolType.Bold,
            SymbolType.Italic, SymbolType.Strike, SymbolType.AnchorReference, SymbolType.WikiWord, SymbolType.Text });
    }

    public static final SymbolType[] literalTableTypes = {
            SymbolType.EndCell, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
    public static final SymbolType[] aliasLinkTypes = {
            SymbolType.CloseBracket, SymbolType.Evaluator, SymbolType.Literal, SymbolType.Variable};
    public static final SymbolType[] linkTargetTypes = {
            SymbolType.Literal, SymbolType.Variable};

    private HashMap<Character, Matchable[]> currentDispatch = dispatch;
    private Matchable[] currentTypes;
    private int currentIndex;

    private Matchable[] getMatchTypes(Character match) {
        if (currentDispatch.containsKey(match)) return currentDispatch.get(match);
        return currentDispatch.get(defaultMatch);
    }

    public SymbolProvider setTypes(Matchable[] types) {
        currentDispatch = new HashMap<Character, Matchable[]>();
        currentDispatch.put(defaultMatch, types);
        return this;
    }

    public SymbolProvider addTypes(SymbolType[] types) {
        ArrayList<Matchable> defaults = new ArrayList<Matchable>();
        defaults.addAll(Arrays.asList(currentDispatch.get(defaultMatch)));
        for (SymbolType type: types) {
            if (!defaults.contains(type)) defaults.add(type);
        }
        Matchable[] newDefaults = new Matchable[defaults.size()];
        for (int i = 0; i < defaults.size(); i++) newDefaults[i] = defaults.get(i);
        currentDispatch.put(defaultMatch, newDefaults);
        return this;
    }

    public boolean hasType(Matchable type) {
        for (Matchable currentType: currentDispatch.get(defaultMatch)) {
            if (type == currentType) return true;
        }
        return false;
    }

    public Iterable<Matchable> candidates(char startsWith) {
        currentTypes = getMatchTypes(startsWith);
        currentIndex = 0;
        return new Iterable<Matchable> () {
            public Iterator<Matchable> iterator() {
                return new Iterator<Matchable>() {

                    public boolean hasNext() {
                        return currentIndex < currentTypes.length;
                    }

                    public Matchable next() {
                        return currentTypes[currentIndex++];
                    }

                    public void remove() {}
                } ;
            }
        };
    }
}
