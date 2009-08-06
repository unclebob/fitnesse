package fitnesse.components;

import fitnesse.wiki.WikiPage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuiteSpecificationMatchFinder extends WikiPageFinder {
  private String titleRegEx;
  private String contentRegEx;

  public SuiteSpecificationMatchFinder(String titleRegEx, String contentRegEx, SearchObserver observer) {
    super(observer);
    this.titleRegEx = titleRegEx;
    this.contentRegEx = contentRegEx;
  }

  protected boolean pageMatches(WikiPage page) throws Exception {
    if(!nullOrEmpty(titleRegEx)&&!nullOrEmpty(contentRegEx))
       return patternMatches(titleRegEx, page.getName())&&patternMatches(contentRegEx,page.getData().getContent());
    else{
    if (patternMatches(titleRegEx, page.getName()))
      return true;
    if (patternMatches(contentRegEx,page.getData().getContent()))
      return true;

    return false;
    }
  }

  private boolean patternMatches(String regEx, String subject) throws Exception {
    if (!nullOrEmpty(regEx)){
      Pattern pattern = Pattern.compile(regEx, Pattern.DOTALL);
      Matcher matcher = pattern.matcher(subject);
      if(matcher.find())
        return true;
    }
    return false;
  }

  private boolean nullOrEmpty(String regEx) {
    return regEx == null || regEx.equals("");
  }
}
