package fitnesse.wikitext.translator;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.Symbol;

public class ContentsBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        HtmlTag div = HtmlUtil.makeDivTag("toc" + 1);
        HtmlTag contentsDiv = HtmlUtil.makeDivTag("contents");
        contentsDiv.add(HtmlUtil.makeBold("Contents:"));
        HtmlTag list = new HtmlTag("ul");
        ItemBuilder itemBuilder = new ItemBuilder(symbol);
        try {
            for (WikiPage child: translator.getPage().getChildren()) {
                HtmlTag listItem = new HtmlTag("li");
                HtmlTag link = HtmlUtil.makeLink(getHref(child), itemBuilder.buildItem(child));
                listItem.add(link);
                list.add(listItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        contentsDiv.add(list);
        div.add(contentsDiv);
        return div.html();
    }

    private String getHref(WikiPage wikiPage) throws Exception {
        //todo: DRY?
        return PathParser.render(wikiPage.getPageCrawler().getFullPath(wikiPage));
    }

    private class ItemBuilder {
        private Symbol contents;

        public ItemBuilder(Symbol contents) {
            this.contents = contents;
        }

        public HtmlElement buildItem(WikiPage page) {
            String itemText = page.getName();
            if (hasOption("-f")) {
                String filters = getFilters(page);
                if (filters.length() > 0) itemText += " (" + filters + ")";
            }
            return new RawHtml(itemText);
        }

        private boolean hasOption(String option) {
            for (Symbol child: contents.getChildren()) {
               if (child.getContent().equals(option)) return true;
            }
            return false;
        }

        private String getFilters(WikiPage wikiPage) {
            try {
                PageData data = wikiPage.getData();
                WikiPageProperties props = data.getProperties();
                if (props.has(PageData.PropertySUITES)) {
                    String propertyValue = props.get(PageData.PropertySUITES);
                    if (propertyValue != null) return propertyValue.trim();
                }
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
    }
}
