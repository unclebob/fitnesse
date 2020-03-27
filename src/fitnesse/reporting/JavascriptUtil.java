package fitnesse.reporting;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.util.StringUtils;

public class JavascriptUtil {

  // backslash, quote, tab, newline, line feed, platform-specific-newline
  private static final String[] specialHtml = new String[]{"\\", "\"", "\t", "\n", "\r", HtmlElement.endl};
  private static final String[] specialHtmlEscapes = new String[]{"\\\\", "\\\"", "\\t", "\\n", "\\r", "\\n"};

  private JavascriptUtil() {
  }


  public static String escapeHtmlForJavaScript(String html) {
    return StringUtils.replaceStrings(html, specialHtml, specialHtmlEscapes);
  }

  public static HtmlTag makeAppendElementScript(String idElement, String htmlToAppend) {
    HtmlTag scriptTag = new HtmlTag("script");
    String escapedIdElement = escapeHtmlForJavaScript(idElement);
    String getElement = "document.getElementById(\"" + escapedIdElement + "\")";
    String escapedHtml = escapeHtmlForJavaScript(htmlToAppend);

    String script = "var existingContent = " + getElement + ".innerHTML;" +
            HtmlTag.endl +
            getElement + ".innerHTML = existingContent + \"" + escapedHtml + "\";" +
            HtmlTag.endl;
    scriptTag.add(script);

    return scriptTag;
  }

  public static HtmlTag makeReplaceElementScript(String idElement, String newHtmlForElement) {
    HtmlTag scriptTag = new HtmlTag("script");
    String escapedIdElement = escapeHtmlForJavaScript(idElement);
    String escapedHtml = escapeHtmlForJavaScript(newHtmlForElement);
    scriptTag.add("document.getElementById(\"" + escapedIdElement + "\").innerHTML = \"" + escapedHtml + "\";");
    return scriptTag;
  }

  public static HtmlTag makeInitErrorMetadataScript() {
    HtmlTag scriptTag = new HtmlTag("script");
    scriptTag.add("initErrorMetadata();");
    return scriptTag;
  }

  public static HtmlTag makeSilentLink(String href, HtmlElement content) {
    HtmlTag link = new HtmlTag("a");
    link.addAttribute("href", "#");
    link.addAttribute("onclick", "doSilentRequest('" + href + "')");
    link.add(content);
    return link;
  }
}
