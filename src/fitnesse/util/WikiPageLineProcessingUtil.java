package fitnesse.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class WikiPageLineProcessingUtil {

  public static boolean isColumnSpecialWikiKeyWord(String stringPassed) {
    stringPassed = stringPassed.trim();
    return stringPassed.isEmpty() || stringPassed.startsWith("$") || stringPassed.equals("ensure") || stringPassed.equals("reject") || stringPassed.equals("check") || stringPassed.equals("check not") || stringPassed.equals("show") || stringPassed.equals("note");
  }

  public static boolean doesLineNeedExtraLastColumn(String stringPassed) {
    String firstColumn = Arrays.asList(stringPassed.split("\\|")).get(1).trim();
    return firstColumn.equals("check") || firstColumn.equals("check not");
  }

  public static String getLastColumn(String stringPassed) {
    if (isValidLine(stringPassed)) {
      List<String> allColumns = Arrays.asList(stringPassed.split("\\|"));
      return allColumns.get(allColumns.size() - 1);
    }
    return "";
  }

  private static boolean isValidLine(String textPassed) {
    return (textPassed.startsWith("|") && textPassed.trim().endsWith("|"));
  }

  public static String getMethodNameFromLine(String textPassed) {
    String methodNameToReturn = "";
    if (isValidLine(textPassed)) {
      List<String> methodSplit = new ArrayList<>();
      methodSplit.addAll(Arrays.asList(textPassed.split("\\|")));
      methodSplit.remove(0);//First one is always empty. We can discard it.
      String firstColumn = methodSplit.get(0);
      int counter = !isColumnSpecialWikiKeyWord(firstColumn) ? 0 : 1;
      boolean lineHasExtraColumn = doesLineNeedExtraLastColumn(textPassed);
      for (int i = counter; i < methodSplit.size(); i += 2) {
        //Ensure last column is not included in the method name.
        if (lineHasExtraColumn && methodSplit.size() == (i + 1)) {
        } else {
          List<String> currentCellWords = Arrays.asList(methodSplit.get(i).trim().split(" "));
          for (String currentCellWord : currentCellWords) {
            if (!currentCellWord.trim().isEmpty()) {
              methodNameToReturn += org.apache.commons.lang3.StringUtils.capitalize(currentCellWord.trim());
            }
          }
        }
      }
      methodNameToReturn = StringUtils.uncapitalize(methodNameToReturn);
    }
    return methodNameToReturn;
  }

  public static LinkedHashMap<Integer, String> getRowColumnsExcludingKeywordInFirstColumnIfPresent(String currentLine) {
    LinkedHashMap<Integer, String> allColumnsOfCurrentLine = new LinkedHashMap<>();
    String remainingText = currentLine;
    int currentIndexOfPipe = 1;
    boolean isFirstColumnSpecialKey = true;
    while (remainingText.contains("|")) {
      int nextIndexOfPipe = currentLine.indexOf("|", currentIndexOfPipe);
      String currentColumnText = currentLine.substring(currentIndexOfPipe, nextIndexOfPipe);
      if (isFirstColumnSpecialKey && !isColumnSpecialWikiKeyWord(currentColumnText)) {
        isFirstColumnSpecialKey = false;
        allColumnsOfCurrentLine.put(currentIndexOfPipe, currentColumnText);
      } else if (!isFirstColumnSpecialKey) {
        allColumnsOfCurrentLine.put(currentIndexOfPipe, currentColumnText);
      }
      currentIndexOfPipe = nextIndexOfPipe + 1;
      remainingText = currentLine.substring(nextIndexOfPipe + 1);
    }
    if (doesLineNeedExtraLastColumn(currentLine))
      allColumnsOfCurrentLine.remove(allColumnsOfCurrentLine.size() - 1);
    return allColumnsOfCurrentLine;
  }

}
