package fitnesse.components;

public interface CompositePageFinder extends PageFinder {

  public abstract void add(PageFinder finder);

}