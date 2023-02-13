package fitnesse.wikitext.parser;

public class ScanString {
    private final CharSequence input;
    private int offset;
    private int markStartOffset;

    public ScanString(CharSequence input, int offset) {
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
    public void moveNext(int length) { offset += length; }
    public boolean isEnd() { return isEnd(0); }
    public boolean isEnd(int startAt) { return offset + startAt >= input.length(); }
    public void markStart(int markStartOffset) { this.markStartOffset = markStartOffset; }

    public boolean matches(String match, int startsAt) {
        if (match.isEmpty()) return false;
        if (offset + startsAt + match.length() > input.length()) return false;
        return match.equals(input.subSequence(offset + startsAt, offset + startsAt + match.length()).toString());
    }

    public boolean startsWith(String match) {
        return matches(match, 0);
    }

    public boolean startsLine(int startAt) {
        return offset + startAt == 0 || offset + startAt == markStartOffset || input.charAt(offset + startAt - 1) == '\n';
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
        return input.subSequence(offset + startAt, offset + endBefore).toString();
    }

    public String rawSubstring(int startAt, int endBefore) {
        return input.subSequence(startAt, endBefore).toString();
    }

    public String substringFrom(int startAt) {
        return input.subSequence(startAt, offset).toString();
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
