package fitnesse.util;

import java.util.Collection;

public interface Tree<T> {
  T getNode();
  Collection<? extends Tree<T>> getBranches();

  default boolean walkPreOrder(TreeWalker<T> walker) {
    if (!walker.visit(getNode())) return false;
    if (walker.visitBranches(getNode())) {
      for (Tree<T> branch: getBranches()) {
        if (!branch.walkPreOrder(walker)) return false;
      }
    }
    return true;
  }

  default boolean walkPostOrder(TreeWalker<T> walker) {
    if (walker.visitBranches(getNode())) {
      for (Tree<T> branch: getBranches()) {
        if (!branch.walkPostOrder(walker)) return false;
      }
    }
    return walker.visit(getNode());
  }
}
