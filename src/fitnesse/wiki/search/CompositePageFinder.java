package fitnesse.wiki.search;

public interface CompositePageFinder extends PageFinder {

  void add(PageFinder finder);

}
