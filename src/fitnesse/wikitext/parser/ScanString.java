package fitnesse.wikitext.parser;

public class ScanString {
    private final String input;
    private int offset;
    private int markStartOffset;

    public ScanString(String input, int offset) {
        this.input = input != null ? input : "";
        this.offset = offset;
    }

    public ScanString(ScanString other) {
        this.input = other.input;
        this.offset = other.offset;
        this.markStartOffset = other.markStartOffset;
    }

    public void setOffset(int offset) { this.offset = offset; }
    public int getOffset() { return offset; }
    public void moveNext() { offset++; }
    public boolean isEnd() { return isEnd(0); }
    public boolean isEnd(int startAt) { return offset + startAt >= input.length(); }
    public void markStart(int markStartOffset) { this.markStartOffset = markStartOffset; }

    public boolean matches(String match, int startsAt) {
        if (match.length() == 0) return false;
        if (offset + startsAt + match.length() > input.length()) return false;
        return input.regionMatches(offset + startsAt, match, 0, match.length());
    }

    public boolean startsWith(String match) {
        return matches(match, 0);
    }

    public boolean startsLine(int startAt) {
        return startsLine(startAt, "\n");
    }

    public boolean startsLine(int startAt, String startDelimiters) {
        return offset + startAt == 0 || offset + startAt == markStartOffset || startDelimiters.indexOf(input.charAt(offset + startAt - 1)) >= 0;
    }

    public int find(char[] matches, int startAt) {
        int current = offset + startAt;
        while (current < input.length()) {
            for (char match: matches) {
                if (input.charAt(current) == match) return current - offset;
            }
            current++;
        }
        return -1;
    }

    public String substring(int startAt, int endBefore) {
        return input.substring(offset + startAt, offset + endBefore);
    }

    public String rawSubstring(int startAt, int endBefore) {
        return input.substring(startAt, endBefore);
    }

    public String substringFrom(int startAt) {
        return input.substring(startAt, offset);
    }

    public char charAt(int startAt) {
        if (offset + startAt >= input.length()) return 0;
        return input.charAt(offset + startAt);
    }

    public int whitespaceLength(int startAt) {
        int current = offset + startAt;
        while (current < input.length()) {
            if (!Character.isWhitespace(input.charAt(current))) break;
            if (input.charAt(current) == '\n' || input.charAt(current) == '\r') break;
            current++;
        }
        return current - offset - startAt;
    }

    public static boolean isWord(String content) {
        for (char c: content.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_') return false;
        }
        return true;
    }

    public static boolean isDigits(String content) {
        for (char c: content.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    public static boolean isVariableName(String content) {
        for (char c: content.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '.') return false;
        }
        return true;
    }
}
