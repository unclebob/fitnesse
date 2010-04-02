package fitnesse.wikitext.parser;

import util.Maybe;
import java.util.ArrayList;

public class Matcher {
    private interface ScanMatch {
        Maybe<Integer> match(ScanString input, int offset);
    }

    private ArrayList<ScanMatch> matches = new ArrayList<ScanMatch>();
    private Class<? extends Rule> ruleClass;

    public Matcher ruleClass(Class<? extends Rule> ruleClass) {
        this.ruleClass = ruleClass;
        return this;
    }

    public Matcher whitespace() {
        matches.add(new ScanMatch() {
            public Maybe<Integer> match(ScanString input, int offset) {
                int length = input.whitespaceLength(offset);
                return length > 0 ? new Maybe<Integer>(length) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher startLine() {
        matches.add(new ScanMatch() {
            public Maybe<Integer> match(ScanString input, int offset) {
                return input.startsLine(offset) ? new Maybe<Integer>(0) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher string(final String delimiter) {
        matches.add(new ScanMatch() {
            public Maybe<Integer> match(ScanString input, int offset) {
                return input.matches(delimiter, offset) ? new Maybe<Integer>(delimiter.length()) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher string(final String[] delimiters) {
        matches.add(new ScanMatch() {
            public Maybe<Integer> match(ScanString input, int offset) {
                for (String delimiter: delimiters) {
                    if (input.matches(delimiter, offset)) return new Maybe<Integer>(delimiter.length());
                }
                return Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher repeat(final char delimiter) {
        matches.add(new ScanMatch() {
            public Maybe<Integer> match(ScanString input, int offset) {
                int size = 0;
                while (input.charAt(offset + size) == delimiter) size++;
                return size > 0 ? new Maybe<Integer>(size) : Maybe.noInteger;
            }
        });
        return this;
    }

    public Matcher endsWith(final char[] terminators) {
        matches.add(new ScanMatch() {
            public Maybe<Integer> match(ScanString input, int offset) {
                int size = 0;
                while (true) {
                    char candidate = input.charAt(offset + size);
                    if (candidate == 0) return Maybe.noInteger;
                    if (contains(terminators, candidate)) break;
                    size++;
                }
                return size > 0 ? new Maybe<Integer>(size + 1) : Maybe.noInteger;
            }

            private boolean contains(char[] terminators, char candidate) {
                for (char terminator: terminators) if (candidate == terminator) return true;
                return false;
            }
        });
        return this;
    }

    public Matcher noMatch() {
        matches.add(new ScanMatch() {
            public Maybe<Integer> match(ScanString input, int offset) {
                return Maybe.noInteger;
            }
        });
        return this;
    }

    public TokenMatch makeMatch(SymbolType type, ScanString input)  {
        int totalLength = 0;
        for (ScanMatch match: matches) {
            Maybe<Integer> matchLength = match.match(input, totalLength);
            if (matchLength.isNothing()) return TokenMatch.noMatch;
            totalLength += matchLength.getValue();
        }

        Token token = new Token();

        if (ruleClass != null) {
            token.setRule(makeProduction());
        }
        token.setType(type);
        token.setContent(input.substring(0, totalLength));
        return new TokenMatch(token, totalLength);
    }

    private Rule makeProduction() {
        try {
            return ruleClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
