package fitnesse.wikitext.parser;

public enum TokenType {
    Whitespace(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            int size = input.whitespaceLength();
            return size > 0 ? new TokenMatch(new Token(type, " "), size) : TokenMatch.noMatch;
        }
    }),

    Table(new StartLineMatcher(new String[] {"|", "!|"}, TableToken.class)),
    Colon(new BasicMatcher(":", Token.class)),
    Comma(new BasicMatcher(",", Token.class)),
    Evaluator(new BasicMatcher("${=", EvaluatorToken.class)),
    CloseEvaluator(new BasicMatcher("=}", Token.class)),
    Variable(new BasicMatcher("${", VariableToken.class)),
    HashTable(new BasicMatcher("!{", HashTableToken.class)),
    Preformat(new BasicMatcher("{{{", PreformatToken.class)),
    ClosePreformat(new StringMatcher("}}}", new Token())),
    OpenParenthesis(new StringMatcher("(", new Token())),
    OpenBrace(new StringMatcher("{", new Token())),
    OpenBracket(new StringMatcher("[", new Token())),
    CloseParenthesis(new StringMatcher(")", new Token())),
    CloseBrace(new StringMatcher("}", new Token())),
    CloseBracket(new StringMatcher("]", new Token())),
    Newline(new StringMatcher("\n", new NewlineToken())),
    OpenLiteral(new BasicMatcher("!-", OpenLiteralToken.class)),
    CloseLiteral(new BasicMatcher("-!", Token.class)),

    Collapsible(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsLine() && input.startsWith("!*")) {
                String bodyStyle ="collapsable";
                int offset = 2;
                while (input.charAt(offset) == '*') offset++;
                if (input.charAt(offset) == '>') {
                    offset++;
                    bodyStyle="hidden";
                }
                return new TokenMatch(new CollapsibleToken(bodyStyle), offset);
            }
            return TokenMatch.noMatch;
        }
    }),

    EndSection(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsLine() && input.startsWith("*")) {
                int offset = 1;
                while (input.charAt(offset) == '*') offset++;
                if (input.charAt(offset) == '!') {
                    return new TokenMatch(new Token(type, input.substring(0, offset + 1)), offset + 1);
                }
            }
            return TokenMatch.noMatch;
        }
    }),

    HorizontalRule(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsWith("----")) {
                int size = 1;
                while (input.charAt(size + 3) == '-') size++;
                return new TokenMatch(new HorizontalRuleToken(Integer.toString(size)), size + 3);
            }
            return TokenMatch.noMatch;
        }
    }),

    Bold(new BasicMatcher("'''", EqualPairToken.class)),
    Italic(new BasicMatcher("''", EqualPairToken.class)),
    Strike(new BasicMatcher("--", EqualPairToken.class)),

    Style(new Matcher() {
        private final String delimiter = "!style_";
        private final char[] beginners = {'(', '{', '['};

        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsWith(delimiter)) {
                int beginner = input.find(beginners, delimiter.length());
                if (beginner > delimiter.length()) {
                    return new TokenMatch(
                            new StyleToken(input.substring(delimiter.length(), beginner),
                                    makeTerminator(input.charAt(beginner))),
                            beginner + 1);
                }
            }
            return TokenMatch.noMatch;
        }

        private TokenType makeTerminator(char beginner) {
            return beginner == '[' ? TokenType.CloseBracket
                    : beginner == '{' ? TokenType.CloseBrace
                    : TokenType.CloseParenthesis;
        }
    }),

    EndCell(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            return input.startsWith("|\n|") ? new TokenMatch(new CellDelimiterToken("|\n|"), 3)
                    : input.startsWith("|\n") ? new TokenMatch(new CellDelimiterToken("|\n"), 2)
                    : input.startsWith("|") ? new TokenMatch(new CellDelimiterToken("|"), 1)
                    : TokenMatch.noMatch;
        }
    }),


    AnchorReference(new BasicMatcher(".#", AnchorReferenceToken.class)),

    HeaderLine(new LineMatcher(new String[] {"1", "2", "3", "4", "5", "6"})),
    AnchorName(new BasicMatcher("!anchor", AnchorNameToken.class)),
    Contents(new StartLineMatcher("!contents", ContentsToken.class)),
    CenterLine(new LineMatcher(new String[] {"c", "C"})),
    Define(new StartLineMatcher("!define", DefineToken.class)),
    Include(new StartLineMatcher("!include", IncludeToken.class)),
    NoteLine(new LineMatcher(new String[] {"note"})),

    Text(new NoMatch()),
    Empty(new NoMatch());

    public static TokenType closeType(TokenType type) {
        return type == OpenBrace ? CloseBrace
                : type == OpenBracket ? CloseBracket
                : type == OpenParenthesis ? CloseParenthesis
                : Empty;
    }

    private Matcher matcher;

    TokenType(Matcher matcher) { this.matcher = matcher; }

    public TokenMatch makeMatch(ScanString input) { return matcher.makeMatch(this, input); }

    private static class BasicMatcher implements Matcher {
        private String delimiter;
        private Class<? extends Token> tokenClass;

        public BasicMatcher(String delimiter, Class<? extends Token> tokenClass) {
            this.delimiter = delimiter;
            this.tokenClass = tokenClass;
        }

        public TokenMatch makeMatch(TokenType type, ScanString input)  {
            if (input.startsWith(delimiter)) {
                Token token;
                try {
                    token = tokenClass.newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException(e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
                token.setType(type);
                token.setContent(delimiter);
                return new TokenMatch(token, delimiter.length());
            }
            return TokenMatch.noMatch;
        }
    }

    private static class BasicTokenMatcher implements Matcher {
        private String delimiter;
        private Token token;

        public BasicTokenMatcher(String delimiter, Token token) {
            this.delimiter = delimiter;
            this.token = token;
        }

        public TokenMatch makeMatch(TokenType type, ScanString input)  {
            if (input.startsWith(delimiter)) {
                token.setType(type);
                token.setContent(delimiter);
                return new TokenMatch(token, delimiter.length());
            }
            return TokenMatch.noMatch;
        }
    }

    private static class StringMatcher implements Matcher {
        private final String match;
        private Token token;

        public StringMatcher(String match, Token token) {
            this.match = match;
            this.token = token;
        }

        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsWith(match)) {
                token.setType(type);
                token.setContent(match);
                return new TokenMatch(token, match.length());
            }
            return TokenMatch.noMatch;
        }
    }

    private static class LineMatcher implements Matcher {
        private final String[] matches;

        public LineMatcher(String[] matches) {
            this.matches = matches;
        }

        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsLine() && input.startsWith("!")) {
                for (String match: matches) {
                if (input.matches(match, 1))
                    return new TokenMatch(new LineToken(match, type), match.length() + 1);
                }
            }
            return TokenMatch.noMatch;
        }
    }

    private static class NoMatch implements Matcher {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            return TokenMatch.noMatch;
        }
    }

}

