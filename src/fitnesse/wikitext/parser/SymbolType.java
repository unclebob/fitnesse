package fitnesse.wikitext.parser;

import java.util.HashMap;

public class SymbolType {
    public static final int WikiMatch = 1;
    
    public static final SymbolType Alias = new SymbolType("Alias").add(WikiMatch, new Matcher().string("[["));
    public static final SymbolType AnchorName = new SymbolType("AnchorName").add(WikiMatch, new Matcher().string("!anchor"));
    public static final SymbolType AnchorReference = new SymbolType("AnchorReference").add(WikiMatch, new Matcher().string(".#"));
    public static final SymbolType Bold = new SymbolType("Bold").add(WikiMatch, new Matcher().string("'''"));
    public static final SymbolType CenterLine = new SymbolType("CenterLine").add(WikiMatch, new Matcher().string(new String[] {"!c", "!C"}));
    public static final SymbolType CloseBrace = new SymbolType("CloseBrace").add(WikiMatch, new Matcher().string("}"));
    public static final SymbolType CloseBracket = new SymbolType("CloseBracket").add(WikiMatch, new Matcher().string("]"));
    public static final SymbolType CloseCollapsible = new SymbolType("CloseCollapsible").add(WikiMatch, new Matcher().string("\n").repeat('*').string("!"));
    public static final SymbolType CloseEvaluator = new SymbolType("CloseEvaluator").add(WikiMatch, new Matcher().string("=}"));
    public static final SymbolType CloseLiteral = new SymbolType("CloseLiteral").add(WikiMatch, new Matcher().string("-!"));
    public static final SymbolType CloseParenthesis = new SymbolType("CloseParenthesis").add(WikiMatch, new Matcher().string(")"));
    public static final SymbolType ClosePlainTextTable = new SymbolType("ClosePlainTextTable").add(WikiMatch, new Matcher().startLine().string("]!"));
    public static final SymbolType ClosePreformat = new SymbolType("ClosePreformat").add(WikiMatch, new Matcher().string("}}}"));
    public static final SymbolType Collapsible = new SymbolType("Collapsible").add(WikiMatch, new Matcher().startLine().string("!").repeat('*'));
    public static final SymbolType Colon = new SymbolType("Colon").add(WikiMatch, new Matcher().string(":"));
    public static final SymbolType Comma = new SymbolType("Comma").add(WikiMatch, new Matcher().string(","));
    public static final SymbolType Comment = new SymbolType("Comment").add(WikiMatch, new Matcher().startLine().string("#"));
    public static final SymbolType Contents = new SymbolType("Contents").add(WikiMatch, new Matcher().string("!contents"));
    public static final SymbolType Define = new SymbolType("Define").add(WikiMatch, new Matcher().startLine().string("!define"));
    public static final SymbolType EMail = new SymbolType("EMail");
    public static final SymbolType Empty = new SymbolType("Empty");
    public static final SymbolType EndCell = new SymbolType("EndCell").add(WikiMatch, new Matcher().string(new String[] {"|\n|", "|\n", "|"}));
    public static final SymbolType Evaluator = new SymbolType("Evaluator").add(WikiMatch, new Matcher().string("${="));
    public static final SymbolType HashTable = new SymbolType("HashTable").add(WikiMatch, new Matcher().string("!{"));
    public static final SymbolType HashRow = new SymbolType("HashRow");
    public static final SymbolType HeaderLine = new SymbolType("HeaderLine").add(WikiMatch, new Matcher().string("!").string(new String[] {"1", "2", "3", "4", "5", "6"}));
    public static final SymbolType HorizontalRule = new SymbolType("HorizontalRule").add(WikiMatch, new Matcher().string("---").repeat('-'));
    public static final SymbolType Image = new SymbolType("Image").add(WikiMatch, new Matcher().string(new String[] {"!img-l", "!img-r", "!img"}));
    public static final SymbolType Include = new SymbolType("Include").add(WikiMatch, new Matcher().startLine().string("!include"));
    public static final SymbolType Italic = new SymbolType("Italic").add(WikiMatch, new Matcher().string("''"));
    public static final SymbolType LastModified = new SymbolType("LastModified").add(WikiMatch, new Matcher().string("!lastmodified"));
    public static final SymbolType Link = new SymbolType("Link").add(WikiMatch, new Matcher().string(new String[] {"http://", "https://"}));
    public static final SymbolType Literal = new SymbolType("Literal").add(WikiMatch, new Matcher().string("!-"));
    public static final SymbolType Meta = new SymbolType("Meta").add(WikiMatch, new Matcher().string("!meta"));
    public static final SymbolType Newline = new SymbolType("Newline").add(WikiMatch, new Matcher().string("\n"));
    public static final SymbolType NoteLine = new SymbolType("NoteLine").add(WikiMatch, new Matcher().string("!note"));
    public static final SymbolType OpenBrace = new SymbolType("OpenBrace").add(WikiMatch, new Matcher().string("{"));
    public static final SymbolType OpenBracket = new SymbolType("OpenBracket").add(WikiMatch, new Matcher().string("["));
    public static final SymbolType OpenParenthesis = new SymbolType("OpenParenthesis").add(WikiMatch, new Matcher().string("("));
    public static final SymbolType OrderedList = new SymbolType("OrderedList").add(WikiMatch, new Matcher().startLine().whitespace().string(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9"}).string(" "));
    public static final SymbolType Path = new SymbolType("Path").add(WikiMatch, new Matcher().startLine().string("!path"));
    public static final SymbolType PlainTextCellSeparator = new SymbolType("PlainTextCellSeparator");
    public static final SymbolType PlainTextTable = new SymbolType("PlainTextTable").add(WikiMatch, new Matcher().startLine().string("!["));
    public static final SymbolType Preformat = new SymbolType("Preformat").add(WikiMatch, new Matcher().string("{{{"));
    public static final SymbolType See = new SymbolType("See").add(WikiMatch, new Matcher().string("!see").whitespace());
    public static final SymbolType Strike = new SymbolType("Strike").add(WikiMatch, new Matcher().string("--"));
    public static final SymbolType Style = new SymbolType("Style").add(WikiMatch, new Matcher().string("!style_").endsWith(new char[] {'(', '{', '['}));
    public static final SymbolType SymbolList = new SymbolType("SymbolList");
    public static final SymbolType Table = new SymbolType("Table").add(WikiMatch, new Matcher().startLine().string(new String[] {"|", "!|", "-|", "-!|"}));
    public static final SymbolType Today = new SymbolType("Today").add(WikiMatch, new Matcher().string("!today"));
    public static final SymbolType Text = new SymbolType("Text");
    public static final SymbolType UnorderedList = new SymbolType("UnorderedList").add(WikiMatch, new Matcher().startLine().whitespace().string("* "));
    public static final SymbolType Variable = new SymbolType("Variable").add(WikiMatch, new Matcher().string("${"));
    public static final SymbolType Whitespace = new SymbolType("Whitespace").add(WikiMatch, new Matcher().whitespace());
    public static final SymbolType WikiWord = new SymbolType("WikiWord");
    
    private String name;
    private HashMap<Integer, Object> attributes = new HashMap<Integer, Object>(); 

    public SymbolType(String name) { this.name = name; }
    
    public boolean hasAttribute(Integer attributeKey) { return attributes.containsKey(attributeKey); }
    public Object getAttribute(Integer attributeKey) { return attributes.get(attributeKey); }
    
    public SymbolType add(Integer attributeKey, Object attributeValue) {
        attributes.put(attributeKey, attributeValue);
        return this;
    }
    
    @Override public String toString() { return name; }
}

