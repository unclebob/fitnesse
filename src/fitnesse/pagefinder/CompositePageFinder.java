package fitnesse.pagefinder;

public interface CompositePageFinder extends PageFinder {

  public abstract void add(PageFinder finder);

}