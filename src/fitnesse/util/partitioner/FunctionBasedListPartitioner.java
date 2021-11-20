package fitnesse.util.partitioner;

import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Splits a list into sublists based on two functions.
 * One will give positions where a preference for a sublist is known beforehand, the o
 * @param <T> list element type
 */
public class FunctionBasedListPartitioner<T> implements ListPartitioner<T> {
  private final Function<T, Optional<Integer>> positionFunction;
  private final BiFunction<List<List<T>>, List<T>, List<List<T>>> notFoundFunction;

  /**
   * Creates new.
   * @param positionFunction indicates sublist index for an element if it can be determined on its own
   * @param notFoundFunction partitions the elements for which the positionFunction did not give an index,
   *                         it gets the partitions created based on the positionFunctions and the elements
   *                         not yet placed in a partition.
   */
  public FunctionBasedListPartitioner(Function<T, Optional<Integer>> positionFunction,
                                      BiFunction<List<List<T>>, List<T>, List<List<T>>> notFoundFunction) {
    this.positionFunction = positionFunction;
    this.notFoundFunction = notFoundFunction;
  }

  @Override
  public List<List<T>> split(List<T> source, int partitionCount) {
    List<List<T>> result = new ArrayList<>(partitionCount);
    for (int j = 0; j < partitionCount; j++) {
      result.add(new LinkedList<>());
    }
    List<T> notFound = addUsingPositionFunction(source, result);
    if (!notFound.isEmpty()) {
      List<List<T>> extraItems = notFoundFunction.apply(result, notFound);
      if (partitionCount < extraItems.size()) {
        throw new IllegalArgumentException("Extra items use too many partitions: " + extraItems.size());
      }
      result = combinePlacedAndNotFound(result, extraItems);
    }
    return result;
  }

  protected List<T> addUsingPositionFunction(List<T> source, List<List<T>> result) {
    int partitionCount = result.size();
    List<T> notFound = new ArrayList<>();
    for (T item : source) {
      Optional<Integer> pos = positionFunction.apply(item);
      if (!pos.isPresent()) {
        pos = findParentPosition(item);
      }
      if (pos.isPresent()) {
        int index = pos.get();
        if (index >= 0 && index < partitionCount) {
          result.get(index).add(item);
        } else {
          notFound.add(item);
        }
      } else {
        notFound.add(item);
      }
    }
    return notFound;
  }

  private Optional<Integer> findParentPosition(T item) {
    if (item instanceof WikiPage) {
      WikiPage wikiPage = ((WikiPage) item).getParent();
      while (!wikiPage.isRoot()) {
        Optional<Integer> pos = positionFunction.apply((T) wikiPage);
        if (pos.isPresent()) {
          return pos;
        }
        wikiPage = wikiPage.getParent();
      }
    }
    return Optional.empty();
  }

  protected List<List<T>> combinePlacedAndNotFound(List<List<T>> partitions, List<List<T>> extraItems) {
    List<List<T>> result = new ArrayList<>(partitions.size());
    for (List<T> placed : partitions) {
      List<T> endPartition = new ArrayList<>(placed);
      result.add(endPartition);
    }

    for (int i = 0; i < extraItems.size(); i++) {
      List<T> extras = extraItems.get(i);
      result.get(i).addAll(0, extras);
    }
    return result;
  }
}
