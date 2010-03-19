package fitnesse.wikitext.parser;

public enum TokenType {
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
                if (input.charAt(offset) == ' ') {
                    return new TokenMatch(new CollapsibleToken(bodyStyle), offset + 1);
                }
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
                    return new TokenMatch(new EndSectionToken(), offset + 1);
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

    Bold(new StringMatcher("'''", EqualPairToken.BoldToken)),
    Italic(new StringMatcher("''", EqualPairToken.ItalicToken)),
    Strike(new StringMatcher("--", EqualPairToken.StrikeToken)),

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

        private DelimiterToken makeTerminator(char beginner) {
            return beginner == '[' ? DelimiterToken.CloseBracketToken
                    : beginner == '{' ? DelimiterToken.CloseBraceToken
                    : DelimiterToken.CloseParenthesisToken;
        }
    }),

    CloseParenthesis(new StringMatcher(")", DelimiterToken.CloseParenthesisToken)),
    CloseBrace(new StringMatcher("}", DelimiterToken.CloseBraceToken)),
    CloseBracket(new StringMatcher("]", DelimiterToken.CloseBracketToken)),

    EndCell(new Matcher() {
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            return input.startsWith("|\n|") ? new TokenMatch(new CellDelimiterToken("|\n|"), 3)
                    : input.startsWith("|\n") ? new TokenMatch(new CellDelimiterToken("|\n"), 2)
                    : input.startsWith("|") ? new TokenMatch(new CellDelimiterToken("|"), 1)
                    : TokenMatch.noMatch;
        }
    }),

    HeaderLine(new LineMatcher(new String[] {"1", "2", "3", "4", "5", "6"}, LineToken.HeaderLine)),
    CenterLine(new LineMatcher(new String[] {"c", "C"}, LineToken.CenterLine)),
    NoteLine(new LineMatcher(new String[] {"note"}, LineToken.NoteLine)),
    Newline(new StringMatcher("\n", new NewlineToken())),

    AnchorName(new Matcher() {
        private final String delimiter = "!anchor ";

        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsWith(delimiter)) {
                int wordLength = input.wordLength(delimiter.length());
                if (wordLength > 0) {
                    return new TokenMatch(
                            new AnchorNameToken(input.substring(delimiter.length(), delimiter.length() + wordLength)),
                            delimiter.length() + wordLength);
                }
            }
            return TokenMatch.noMatch;
        }
    }),

    AnchorReference(new Matcher() {
        private final String delimiter = ".#";
        
        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsWith(delimiter)) {
                int wordLength = input.wordLength(delimiter.length());
                if (wordLength > 0) {
                    return new TokenMatch(
                            new AnchorReferenceToken(input.substring(delimiter.length(), delimiter.length() + wordLength)),
                            delimiter.length() + wordLength);
                }
            }
            return TokenMatch.noMatch;
        }

    }),

    Text(new NoMatch()),
    Empty(new NoMatch());

    private Matcher matcher;

    TokenType(Matcher matcher) { this.matcher = matcher; }

    public TokenMatch makeMatch(ScanString input) { return matcher.makeMatch(this, input); }

    private static class StringMatcher implements Matcher {
        private final String match;
        private ContentTypeToken token;

        public StringMatcher(String match, ContentTypeToken token) {
            this.match = match;
            this.token = token;
        }

        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsWith(match)) {
                 token.setType(type);
                 return new TokenMatch(token, match.length());
            }
            return TokenMatch.noMatch;
        }
    }

    private static class LineMatcher implements Matcher {
        private final String[] matches;

        public LineMatcher(String[] matches, TokenType type) {
            this.matches = matches;
        }

        public TokenMatch makeMatch(TokenType type, ScanString input) {
            if (input.startsLine() && input.startsWith("!")) {
                int blank = input.find(new char[] {' '}, 1);
                if (blank > 1) {
                    String content = input.substring(1, blank);
                    for (String match: matches) {
                    if (match.equals(content))
                        return new TokenMatch(new LineToken(content, type), content.length() + 2);
                    }
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

