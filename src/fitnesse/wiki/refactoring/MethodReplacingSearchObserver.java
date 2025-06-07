package fitnesse.wiki.refactoring;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.Iterator;
import java.util.LinkedHashMap;

import static fitnesse.util.WikiPageLineProcessingUtil.*;

public class MethodReplacingSearchObserver implements TraversalListener<WikiPage> {

  private String searchMethodString;
  private String replacingMethodString;

  public MethodReplacingSearchObserver(String searchString, String replacement) {
    this.searchMethodString = searchString;
    this.replacingMethodString = replacement;
  }

  @Override
  public void process(WikiPage page) {
    PageData pageData = page.getData();

    String content = pageData.getContent();
    String[] lines = content.split(PageData.PAGE_LINE_SEPARATOR);
    String newPageContent = "";
    boolean isModified = false;
    String targetMethod = getMethodNameFromLine(searchMethodString);
    for (String eachLineOfFile : lines) {
      if (!eachLineOfFile.startsWith("|") || !getMethodNameFromLine(eachLineOfFile).equals(targetMethod)) {
        newPageContent += eachLineOfFile + PageData.PAGE_LINE_SEPARATOR;
      } else {
        isModified = true;
        String modifiedLine = "";
        LinkedHashMap<Integer, String> toBeReplacedText = getRowColumnsExcludingKeywordInFirstColumnIfPresent(eachLineOfFile);
        Iterator<Integer> toBeReplacedKeys = toBeReplacedText.keySet().iterator();
        LinkedHashMap<Integer, String> toReplaceText = getRowColumnsExcludingKeywordInFirstColumnIfPresent(replacingMethodString);
        Iterator<Integer> toReplaceKeys = toReplaceText.keySet().iterator();
        for (int i = 0; toBeReplacedKeys.hasNext() || toReplaceKeys.hasNext(); i++) {
          int toBeReplacedIndex = toBeReplacedKeys.hasNext() ? toBeReplacedKeys.next() : -1;
          int toReplaceIndex = toReplaceKeys.hasNext() ? toReplaceKeys.next() : -1;
          modifiedLine = modifiedLine.isEmpty() ? eachLineOfFile.substring(0, toBeReplacedIndex - 1) : modifiedLine;
          if (toBeReplacedIndex > 0 && toReplaceIndex > 0) {
            if (i % 2 == 1) {
              modifiedLine += "|" + eachLineOfFile.substring(toBeReplacedIndex, toBeReplacedIndex + toBeReplacedText.get(toBeReplacedIndex).length());
            } else {
              modifiedLine += "|" + toReplaceText.get(toReplaceIndex);
            }
          }
          //Below case is when there are more columns in replacing text than in original text.
          else if (toBeReplacedIndex == -1) {
            //All remaining columns of replacing text should be appended.
            modifiedLine += "|" + toReplaceText.get(toReplaceIndex);
            while (toReplaceKeys.hasNext()) {
              modifiedLine += "|" + toReplaceText.get(toReplaceKeys.next());
            }
            break;
          }
          //below case is when there are more columns in original text which need to be removed.
          else if (toReplaceIndex == -1) {
            modifiedLine += "|";
            break;
          }
        }
        if (doesLineNeedExtraLastColumn(eachLineOfFile)) {
          modifiedLine += getLastColumn(eachLineOfFile);
        }
        modifiedLine = (!modifiedLine.isEmpty() && !modifiedLine.endsWith("|")) ? modifiedLine + "|" : modifiedLine;
        newPageContent += modifiedLine + PageData.PAGE_LINE_SEPARATOR;
      }
    }
    if (isModified) {
      pageData.setContent(newPageContent);
    }
    page.commit(pageData);
  }

}
