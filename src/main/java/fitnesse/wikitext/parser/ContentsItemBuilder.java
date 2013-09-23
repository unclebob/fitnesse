package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiImportProperty;
import util.GracefulNamer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ContentsItemBuilder {
    private Symbol contents;
    private int level;

    public ContentsItemBuilder(Symbol contents, int level) {
        this.contents = contents;
        this.level = level;
    }

    public HtmlTag buildLevel(SourcePage page, HtmlTag contentsDiv) {
        HtmlTag div = HtmlUtil.makeDivTag("toc" + level);
        HtmlTag list = new HtmlTag("ul");
        try {
            for (SourcePage child: getSortedChildren(page)) {
                list.add(buildListItem(child));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        contentsDiv.add(list);
        div.add(contentsDiv);
        return div;
    }

    private HtmlTag buildListItem(SourcePage child) {
        HtmlTag listItem = new HtmlTag("li");
        HtmlTag childItem = buildItem(child);
        listItem.add(childItem);
        if (child.getChildren().size() > 0) {
            if (level < getRecursionLimit()) {
                HtmlTag nestedDiv =  HtmlUtil.makeDivTag("nested-contents");
                listItem.add(new ContentsItemBuilder(contents, level + 1).buildLevel(child, nestedDiv));
            }
            else if (getRecursionLimit() > 0){
                childItem.add(contents.getVariable(Contents.MORE_SUFFIX_TOC, Contents.MORE_SUFFIX_DEFAULT));
            }
        }
        return listItem;
    }

    private Collection<SourcePage> getSortedChildren(SourcePage parent) {
        ArrayList<SourcePage> result = new ArrayList<SourcePage>(parent.getChildren());
        Collections.sort(result);
        return result;
    }

    public HtmlTag buildItem(SourcePage page) {
        HtmlTag result = new HtmlTag("a", buildBody(page));
        result.addAttribute("href", buildReference(page));
        String help = page.getProperty(PageData.PropertyHELP);
        if (help.length() > 0) {
            if (hasOption("-h", Contents.HELP_TOC)) {
                result.tail = HtmlUtil.makeSpanTag("pageHelp", ": " + help).htmlInline();
            }
            else {
                result.addAttribute("title", help);
            }
        }
        return result;
    }

    private String buildBody(SourcePage page) {
        String itemText = page.getName();

        if (hasOption("-g", Contents.REGRACE_TOC)) {
            //todo: DRY? see wikiwordbuilder
            itemText = GracefulNamer.regrace(itemText);
        }

        if (hasOption("-p", Contents.PROPERTY_TOC)) {
            String properties = getBooleanProperties(page);
            if (properties.length() > 0) itemText += " " + properties;
        }

        if (hasOption("-f", Contents.FILTER_TOC)) {
            String filters = page.getProperty(PageData.PropertySUITES);
            if (filters.length() > 0) itemText += " (" + filters + ")";
        }
        
        return itemText;
    }

    private String buildReference(SourcePage sourcePage) {
        return sourcePage.getFullName();
    }

    private int getRecursionLimit() {
        for (Symbol child: contents.getChildren()) {
            if (!child.getContent().startsWith("-R")) continue;
            String level = child.getContent().substring(2);
            if (level.length() == 0) return Integer.MAX_VALUE;
            try {
              return Integer.parseInt(level);
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private boolean hasOption(String option, String variableName) {
        for (Symbol child: contents.getChildren()) {
           if (child.getContent().equals(option)) return true;
        }
        return variableName.length() > 0
                && contents.getVariable(variableName, "").equals("true");
    }

    private String getBooleanProperties(SourcePage sourcePage) {
        String result = "";
        if (sourcePage.hasProperty(PageType.SUITE.toString())) result += "*";
        if (sourcePage.hasProperty(PageType.TEST.toString())) result += "+";
        if (sourcePage.hasProperty(WikiImportProperty.PROPERTY_NAME)) result += "@";
        if (sourcePage.hasProperty(PageData.PropertyPRUNE)) result += "-";
        return result;
    }
}
