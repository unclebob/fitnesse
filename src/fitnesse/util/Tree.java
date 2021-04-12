package fitnesse.util;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Tree<T extends Tree<T>> {

  protected abstract T getNode() ;
  protected abstract List<T> getBranches();

  public boolean isLeaf() { return getBranches().size() == 0; }
  public T getBranch(int child) { return getBranches().get(child); }
  public T getLastBranch() { return getBranches().get(getBranches().size() - 1); }

  public void walkPreOrder(Consumer<T> visitNode, Predicate<T> visitBranches) {
    visitNode.accept(getNode());
    if (visitBranches.test(getNode())) {
      for (T branch : getBranches()) {
        branch.walkPreOrder(visitNode, visitBranches);
      }
    }
  }

  public void walkPreOrder(Consumer<T> visitNode) {
    walkPreOrder(visitNode, node -> true);
  }

  public void walkPostOrder(Consumer<T> visitNode, Predicate<T> visitBranches) {
    if (visitBranches.test(getNode())) {
      for (T branch : getBranches()) {
        branch.walkPostOrder(visitNode, visitBranches);
      }
    }
    visitNode.accept(getNode());
  }

  public void walkPostOrder(Consumer<T> visitNode) {
    walkPostOrder(visitNode, node -> true);
  }

  public <X, U> U collectBranches(Function<T, X> translator, U initial, BiConsumer<U, ? super X> accumulator) {
    getBranches().stream()
      .map(translator)
      .forEachOrdered(item -> accumulator.accept(initial, item));
    return initial;
  }
}
