package fitnesse.util;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Tree<T> {
  T getNode();
  Collection<? extends Tree<T>> getBranches();

  default void walkPreOrder(Consumer<T> visitNode, Predicate<T> visitBranches) {
    visitNode.accept(getNode());
    if (visitBranches.test(getNode())) {
      for (Tree<T> branch : getBranches()) {
        branch.walkPreOrder(visitNode, visitBranches);
      }
    }
  }

  default void walkPreOrder(Consumer<T> visitNode) {
    walkPreOrder(visitNode, node -> true);
  }

  default void walkPostOrder(Consumer<T> visitNode, Predicate<T> visitBranches) {
    if (visitBranches.test(getNode())) {
      for (Tree<T> branch : getBranches()) {
        branch.walkPostOrder(visitNode, visitBranches);
      }
    }
    visitNode.accept(getNode());
  }

  default void walkPostOrder(Consumer<T> visitNode) {
    walkPostOrder(visitNode, node -> true);
  }
}
