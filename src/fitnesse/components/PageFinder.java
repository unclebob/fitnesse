package fitnesse.components;

import java.util.List;

import fitnesse.wiki.WikiPage;

public interface PageFinder {

  public abstract List<WikiPage> search(WikiPage page) throws Exception;

}