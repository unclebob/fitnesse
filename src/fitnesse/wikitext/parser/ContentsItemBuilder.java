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
    private SourcePage page;

    public ContentsItemBuilder(Symbol contents, int level) {
        this(contents, level, null);
    }

    public ContentsItemBuilder(Symbol contents, int level, SourcePage page) {
        this.contents = contents;
        this.level = level;
        this.page = page;
    }

    public HtmlTag buildLevel(SourcePage page) {
        HtmlTag list = new HtmlTag("ul");
        list.addAttribute("class", "toc" + level);
        for (SourcePage child: getSortedChildren(page)) {
            list.add(buildListItem(child));
        }
        return list;
    }

    private HtmlTag buildListItem(SourcePage child) {
        HtmlTag listItem = buildItem(child);
        if (!child.getChildren().isEmpty()) {
            if (level < getRecursionLimit()) {
                listItem.add(new ContentsItemBuilder(contents, level + 1, child).buildLevel(child));
            }
            else if (getRecursionLimit() > 0){
                listItem.add(contents.getVariable(Contents.MORE_SUFFIX_TOC, Contents.MORE_SUFFIX_DEFAULT));
            }
        }
        return listItem;
    }

    private Collection<SourcePage> getSortedChildren(SourcePage parent) {
        ArrayList<SourcePage> result = new ArrayList<>(parent.getChildren());
        Collections.sort(result);
        return result;
    }

    public HtmlTag buildItem(SourcePage page) {
        HtmlTag listItem = new HtmlTag("li");
        HtmlTag link = new HtmlTag("a", buildBody(page));
        link.addAttribute("href", buildReference(page));
        link.addAttribute("class", getBooleanPropertiesClasses(page));
        listItem.add(link);
        String help = page.getProperty(PageData.PropertyHELP);
        if (!help.isEmpty()) {
            if (hasOption("-h", Contents.HELP_TOC)) {
                listItem.add(HtmlUtil.makeSpanTag("pageHelp", ": " + help));
            }
            else if (hasOption("-H", Contents.HELP_INSTEAD_OF_TITLE_TOC)) {
                link.use(help);
            }
            else {
                link.addAttribute("title", help);
            }
        }
        return listItem;
    }

    private String buildBody(SourcePage page) {
        String itemText = page.getName();

        if (hasOption("-g", Contents.REGRACE_TOC)) {
            //todo: DRY? see wikiwordbuilder
            itemText = GracefulNamer.regrace(itemText);
        }

        if (hasOption("-p", Contents.PROPERTY_TOC)) {
            String properties = getBooleanProperties(page);
            if (!properties.isEmpty()) itemText += " " + properties;
        }

        if (hasOption("-f", Contents.FILTER_TOC)) {
            String filters = page.getProperty(PageData.PropertySUITES);
            if (!filters.isEmpty()) itemText += " (" + filters + ")";
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
            if (level.isEmpty()) return Integer.MAX_VALUE;
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
        return !variableName.isEmpty()
                && contents.getVariable(variableName, "").equals("true");
    }

    private String getBooleanProperties(SourcePage sourcePage) {
        String propChars = contents.getVariable(Contents.PROPERTY_CHARACTERS,
                Contents.PROPERTY_CHARACTERS_DEFAULT).trim();
        if(propChars.length() != Contents.PROPERTY_CHARACTERS_DEFAULT.length() ){
            propChars = Contents.PROPERTY_CHARACTERS_DEFAULT;
        }

        String result = "";
        if (sourcePage.hasProperty(PageType.SUITE.toString())) result += propChars.charAt(0);
        if (sourcePage.hasProperty(PageType.TEST.toString())) result += propChars.charAt(1);
        if (sourcePage.hasProperty(WikiImportProperty.PROPERTY_NAME)) result += propChars.charAt(2);
        if (page != null && page instanceof WikiSourcePage){
            if (((WikiSourcePage)page).hasSymbolicLinkChild(sourcePage.getName())) result += propChars.charAt(3);
        }
        if (sourcePage.hasProperty(PageData.PropertyPRUNE)) result += propChars.charAt(4);
        return result;
    }
    private String getBooleanPropertiesClasses(SourcePage sourcePage) {
        String result = "";
        if (sourcePage.hasProperty(PageType.SUITE.toString())) {
        		result += "suite";
        	}
        else if (sourcePage.hasProperty(PageType.TEST.toString())) {
        	result += "test";
        	}
        else {
        	result += "static";
        }
        if (sourcePage.hasProperty(WikiImportProperty.PROPERTY_NAME)) result += " linked";
        if (sourcePage.hasProperty(PageData.PropertyPRUNE)) result += " pruned";
        return result;
    }
}
