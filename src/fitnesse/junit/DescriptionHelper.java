package fitnesse.junit;

import fitnesse.testsystems.TestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper to deal with jUnit descriptions.
 */
public class DescriptionHelper {
  // private constructor to  prevent instances from being made
  private DescriptionHelper() {
  }

  /**
   * @param description description of current test.
   * @return current wiki page (null if no wiki page was found in description)
   */
  public static WikiPage getWikiPage(Description description) {
    WikiPage result = null;
    FitNessePageAnnotation pageAnn = description.getAnnotation(FitNessePageAnnotation.class);
    if (pageAnn != null) {
      result = pageAnn.getWikiPage();
    }
    return result;
  }

  /**
   * @param description description of current test.
   * @return current test page (null if no test page was found in description)
   */
  public static TestPage getTestPage(Description description) {
    TestPage result = null;
    FitNessePageAnnotation pageAnn = description.getAnnotation(FitNessePageAnnotation.class);
    if (pageAnn != null) {
      result = pageAnn.getTestPage();
    }
    return result;
  }

  /**
   * @param description description of current test.
   * @return tags for current wiki page (empty list if none)
   */
  public static List<String> getPageTags(Description description) {
    List<String> result = Collections.emptyList();
    WikiPage wikiPage = getWikiPage(description);
    if (wikiPage != null) {
      result = getPageTags(wikiPage);
    }
    return result;
  }

  /**
   * @param page page to get tags for
   * @return tags of the wiki page.
   */
  public static List<String> getPageTags(WikiPage page) {
    List<String> result = Collections.emptyList();

    PageData data = page.getData();
    if (data != null) {
      String suitesValue = StringUtils.stripToNull(data.getProperties().get(PageData.PropertySUITES));
      if (suitesValue != null) {
        result = Arrays.asList(suitesValue.split("\\s*,\\s*"));
      }
    }
    return result;
  }
}
