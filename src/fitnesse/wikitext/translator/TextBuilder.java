package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.parser.Symbol;
import util.StringUtil;

import java.util.Arrays;

public class TextBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        if (!isWikiWordPath(symbol.getContent())) return new HtmlText(symbol.getContent()).html();

        HtmlTag link = new HtmlTag("a", new HtmlText(symbol.getContent()));
        link.addAttribute("href", qualifiedName(translator.getPage(), symbol.getContent()));
        return link.html();
    }

    private String qualifiedName(WikiPage page, String content) {
        String wikiWordPath = makePath(page, content);
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        WikiPagePath fullPathOfWikiWord;
        try {
            WikiPage parentPage = page.getParent();
            fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return PathParser.render(fullPathOfWikiWord);
    }

    public String makePath(WikiPage page, String content) {
        if (content.startsWith("^") || content.startsWith(">")) {
            return makeChildPath(page, content);
        }
        if (content.startsWith("<")) {
            return makeParentPath(page, content);
        }
        return content;
    }

    private String makeParentPath(WikiPage page, String content) {
        String undecoratedPath = content.substring(1);
        String[] pathElements = undecoratedPath.split("\\.");
        String target = pathElements[0];
        PageCrawler crawler = page.getPageCrawler();
        try {
            WikiPage ancestor = crawler.findAncestorWithName(page, target);
            if (ancestor != null) {
                pathElements[0] = PathParser.render(crawler.getFullPath(ancestor));
                return "." + StringUtil.join(Arrays.asList(pathElements), ".");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return "." + undecoratedPath;
    }

    private String makeChildPath(WikiPage page, String content) {
        return String.format("%s.%s", page.getName(), content.substring(1));
    }

    private boolean isWikiWordPath(String content) {
        String candidate = content + ".";
        int offset = "<>^.".indexOf(candidate.substring(0, 1)) >= 0 ? 1 : 0;
        while (offset < candidate.length()) {
            int dot = candidate.indexOf(".", offset);
            if (!isWikiWord(candidate.substring(offset, dot))) return false;
            offset = dot + 1;
        }
        return true;
    }

    private boolean isWikiWord(String candidate) {
        if (candidate.length() < 3) return false;
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

    private boolean isDigit(String candidate, int offset) {
        return Character.isDigit(candidate.charAt(offset));
    }
    private boolean isLetter(String candidate, int offset) {
        return Character.isLetter(candidate.charAt(offset));
    }

    private boolean isCharacter(String candidate, char character, int offset) { return candidate.charAt(offset) == character; }
}
