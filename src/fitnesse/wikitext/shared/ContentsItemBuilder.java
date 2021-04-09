package fitnesse.wikitext.shared;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiSourcePage;
import fitnesse.wikitext.SourcePage;
import util.GracefulNamer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class ContentsItemBuilder {
    private final PropertySource contents;
    private final int level;
    private final SourcePage page;

    public ContentsItemBuilder(PropertySource contents, int level) {
        this(contents, level, null);
    }

    public ContentsItemBuilder(PropertySource contents, int level, SourcePage page) {
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
                listItem.add(contents.findProperty(Names.MORE_SUFFIX_TOC, Names.MORE_SUFFIX_DEFAULT));
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
        String help = page.getProperty(WikiPageProperty.HELP);
        if (!help.isEmpty()) {
            if (hasOption("-h", Names.HELP_TOC)) {
                listItem.add(HtmlUtil.makeSpanTag("pageHelp", ": " + help));
            }
            else if (hasOption("-H", Names.HELP_INSTEAD_OF_TITLE_TOC)) {
                link.use(help);
            }
            else {
                link.addAttribute("title", help);
            }
        }
        return listItem;
    }
    private boolean isSpecialPageToBeCountedAsTest(SourcePage page){
      String pageName = page.getName();
      return pageName.contains("SuiteSetUp") || pageName.contains("SuiteTearDown");
    }

    private int getTotalTestPagesInASuite(SourcePage page) {
      if (page.hasProperty(PageType.TEST.toString()) || isSpecialPageToBeCountedAsTest(page)){
        return 1;
      }
      int counter = 0;
      if (page.hasProperty(PageType.SUITE.toString())) {
        Iterator<SourcePage> pages = page.getChildren().iterator();
        while (pages.hasNext()) {
          SourcePage sourcePage = pages.next();
          counter += getTotalTestPagesInASuite(sourcePage);
        }
      }
      return counter;
    }

    private String buildBody(SourcePage page) {
        String itemText = page.getName();
        //Will show count of test pages under this suite
        if (hasOption("-c", Names.TEST_PAGE_COUNT_TOC)) {
          if (page.hasProperty(PageType.SUITE.toString()))
            itemText += " ( " + getTotalTestPagesInASuite(page) + " )";
        }

        if (hasOption("-g", Names.REGRACE_TOC)) {
            //todo: DRY? see wikiwordbuilder
            itemText = GracefulNamer.regrace(itemText);
        }

        if (hasOption("-p", Names.PROPERTY_TOC)) {
            String properties = getBooleanProperties(page);
            if (!properties.isEmpty()) itemText += " " + properties;
        }

        if (hasOption("-f", Names.FILTER_TOC)) {
            String filters = page.getProperty(WikiPageProperty.SUITES);
            if (!filters.isEmpty()) itemText += " (" + filters + ")";
        }

        return itemText;
    }

    private String buildReference(SourcePage sourcePage) {
        return sourcePage.getFullName();
    }

    private int getRecursionLimit() {
      String level = contents.findProperty("-R", "0");
      try {
        return Integer.parseInt(level);
      } catch (NumberFormatException e) {
        return 0;
      }
    }

    private boolean hasOption(String option, String variableName) {
      return contents.hasProperty(option) ||
        (!variableName.isEmpty()
          && contents.findProperty(variableName, "").equals("true"));
    }

    private String getBooleanProperties(SourcePage sourcePage) {
        String propChars = contents.findProperty(Names.PROPERTY_CHARACTERS,
                PROPERTY_CHARACTERS_DEFAULT).trim();
        if(propChars.length() != PROPERTY_CHARACTERS_DEFAULT.length() ){
            propChars = PROPERTY_CHARACTERS_DEFAULT;
        }

        String result = "";
        if (sourcePage.hasProperty(PageType.SUITE.toString())) result += propChars.charAt(0);
        if (sourcePage.hasProperty(PageType.TEST.toString())) result += propChars.charAt(1);
        if (sourcePage.hasProperty(WikiImportProperty.PROPERTY_NAME)) result += propChars.charAt(2);
        if (page != null && page instanceof WikiSourcePage){
            if (((WikiSourcePage)page).hasSymbolicLinkChild(sourcePage.getName())) result += propChars.charAt(3);
        }
        if (sourcePage.hasProperty(WikiPageProperty.PRUNE)) result += propChars.charAt(4);
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
        if (sourcePage.hasProperty(WikiPageProperty.PRUNE)) result += " pruned";
        return result;
    }

    private static final String PROPERTY_CHARACTERS_DEFAULT = "*+@>-";
}
