package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuiteSpecificationMatchFinder extends WikiPageFinder {
  private String titleRegEx;
  private String contentRegEx;

  public SuiteSpecificationMatchFinder(String titleRegEx, String contentRegEx, TraversalListener<? super WikiPage> observer) {
    super(observer);
    this.titleRegEx = titleRegEx;
    this.contentRegEx = contentRegEx;
  }

  protected boolean pageMatches(WikiPage page) {
    if(!nullOrEmpty(titleRegEx) && !nullOrEmpty(contentRegEx))
       return patternMatches(titleRegEx, page.getName()) && patternMatches(contentRegEx,page.getData().getContent());
    else{
      return patternMatches(titleRegEx, page.getName()) || patternMatches(contentRegEx,page.getData().getContent());
    }
  }

  private boolean patternMatches(String regEx, String subject) {
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
