package fitnesse.wikitext.parser;

import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import util.Maybe;

public class TextToken extends Token {
    public TextToken(String content) { super(content); }

    public Maybe<String> render(Scanner scanner) {
        if (!isWikiWordPath()) return new Maybe<String>(getContent());
        return new Maybe<String>(HtmlUtil.makeLink(qualifiedName(), getContent()).html());
    }

    private String qualifiedName() {
        WikiPagePath pathOfWikiWord = PathParser.parse(getContent());
        WikiPagePath fullPathOfWikiWord;
        try {
            WikiPage parentPage = getPage().getParent();
            fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return PathParser.render(fullPathOfWikiWord);
    }

    private boolean isWikiWordPath() {
        String candidate = getContent() + ".";
        int offset = "<>^.".indexOf(candidate.substring(0, 1)) >= 0 ? 1 : 0;
        while (offset < candidate.length()) {
            int dot = candidate.indexOf(".", offset);
            if (!isWikiWord(candidate.substring(offset, dot))) return false;
            offset = dot + 1;
        }
        return true;
    }

    private boolean isWikiWord(String candidate) {
        if (!isUpperCaseLetter(candidate, 0)) return false;
        if (!isDigit(candidate, 1) && !isLowerCaseLetter(candidate, 1)) return false;

        boolean includesUpperCaseLetter = false;
        for (int i = 2; i < candidate.length(); i++) {
            if (isUpperCaseLetter(candidate, i)) includesUpperCaseLetter =  true;
            else if (!isDigit(candidate, i) && !isLetter(candidate, i) && !isCharacter(candidate, '.', i)) return false;
        }
        return includesUpperCaseLetter;
    }

    private boolean isUpperCaseLetter(String candidate, int offset) {
        return isLetter(candidate, offset) && Character.isUpperCase(candidate.charAt(offset));
    }

    private boolean isLowerCaseLetter(String candidate, int offset) {
        return isLetter(candidate, offset) && Character.isLowerCase(candidate.charAt(offset));
    }

    private boolean isDigit(String candidate, int offset) { return Character.isDigit(candidate.charAt(offset)); }
    private boolean isLetter(String candidate, int offset) { return Character.isLetter(candidate.charAt(offset)); }
    private boolean isCharacter(String candidate, char character, int offset) { return candidate.charAt(offset) == character; }

    public TokenType getType() { return TokenType.Text; }
}
