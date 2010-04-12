package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.widgets.TOCWidget;

public class ContentsItemBuilder {
    private Symbol contents;
    private VariableSource variables;

    public ContentsItemBuilder(Symbol contents, VariableSource variables) {
        this.contents = contents;
        this.variables = variables;
    }

    public HtmlTag buildItem(WikiPage page) {
        HtmlTag result = new HtmlTag("a", buildBody(page));
        result.addAttribute("href", BuildReference(page));
        String help = getPageProperties(page, PageData.PropertyHELP);
        if (help.length() > 0) {
            if (hasOption("-h", TOCWidget.HELP_TOC)) {
                result.tail = HtmlUtil.makeSpanTag("pageHelp", ": " + help).htmlInline();
            }
            else {
                result.addAttribute("title", help);
            }
        }
        return result;
    }

    private String buildBody(WikiPage page) {
        String itemText = page.getName();
        if (hasOption("-f", TOCWidget.FILTER_TOC)) {
            String filters = getPageProperties(page, PageData.PropertySUITES);
            if (filters.length() > 0) itemText += " (" + filters + ")";
        }
        return itemText;
    }

    private String BuildReference(WikiPage wikiPage)  {
        //todo: DRY? see wikiwordbuilder
        try {
            return PathParser.render(wikiPage.getPageCrawler().getFullPath(wikiPage));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    private boolean hasOption(String option, String variableName) {
        for (Symbol child: contents.getChildren()) {
           if (child.getContent().equals(option)) return true;
        }
        return variables.findVariable(variableName).getValue().equals("true");
    }

    private String getPageProperties(WikiPage wikiPage, String propertyName) {
        try {
            PageData data = wikiPage.getData();
            WikiPageProperties props = data.getProperties();
            if (props.has(propertyName)) {
                String propertyValue = props.get(propertyName);
                if (propertyValue != null) return propertyValue.trim();
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
