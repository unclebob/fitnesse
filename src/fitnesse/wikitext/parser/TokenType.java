package fitnesse.wikitext.parser;

public enum TokenType {
    Whitespace(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            int size = input.whitespaceLength();
            return size > 0 ? new TokenMatch(new Token(type, " "), size) : TokenMatch.noMatch;
        }
    }),

    Word(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            int size = input.wordLength(0);
            return size > 0 ? new TokenMatch(new Token(type, input.substring(0, size)), size) : TokenMatch.noMatch;
        }
    }),

    Table(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            return input.startsLine() && input.startsWith("|") ?
                new TokenMatch(new TableToken(), 1) :
                TokenMatch.noMatch;
        }
    }),

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

    Bold(new BasicTokenMatcher("'''", EqualPairToken.BoldToken)),
    Italic(new BasicTokenMatcher("''", EqualPairToken.ItalicToken)),
    Strike(new BasicTokenMatcher("--", EqualPairToken.StrikeToken)),
    CloseParenthesis(new StringMatcher(")", new Token())),
    CloseBrace(new StringMatcher("}", new Token())),
    CloseBracket(new StringMatcher("]", new Token())),
    Newline(new StringMatcher("\n", new NewlineToken())),

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

    HeaderLine(new LineMatcher(new String[] {"1", "2", "3", "4", "5", "6"})),
    CenterLine(new LineMatcher(new String[] {"c", "C"})),
    NoteLine(new LineMatcher(new String[] {"note"})),

    AnchorName(new BasicMatcher("!anchor", AnchorNameToken.class)),
    AnchorReference(new BasicMatcher(".#", AnchorReferenceToken.class)),

    Include(new StartLineMatcher("!include", IncludeToken.class)),

    Text(new NoMatch()),
    Empty(new NoMatch());

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

