package fitnesse.util;

public interface TreeWalker<T> {
  boolean visit(T node);
  boolean visitBranches(T node);
}
