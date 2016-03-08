package fitnesse.reporting;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;

public class JavascriptUtil {

  private JavascriptUtil() {
  }


  public static String escapeHtmlForJavaScript(String html) {
    html = html.replaceAll("\\\\", "\\\\\\\\"); // backslash
    html = html.replaceAll("\"", "\\\\\""); //  quote
    html = html.replaceAll("\t", "\\\\t"); // tab
    html = html.replaceAll("\n", "\\\\n"); // newline
    html = html.replaceAll("\r", "\\\\r"); // line feed
    html = html.replaceAll(HtmlElement.endl, "\\\\n");
    return html;
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
