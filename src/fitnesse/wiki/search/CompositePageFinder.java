package fitnesse.wiki.search;

public interface CompositePageFinder extends PageFinder {

  public abstract void add(PageFinder finder);

}