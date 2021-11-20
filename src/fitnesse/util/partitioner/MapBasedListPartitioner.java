package fitnesse.util.partitioner;

import fitnesse.wiki.WikiPage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Partitions list based on map indicating known positions for elenents.
 * @param <T> list element type
 */
public class MapBasedListPartitioner<T> extends FunctionBasedListPartitioner<T> {
  private final Map<String, Integer> positionMap;

  public MapBasedListPartitioner(Function<T, String> keyFunction, Map<String, Integer> positionMap) {
    this(keyFunction, positionMap, (parts, nf) -> new EqualLengthListPartitioner<T>().split(nf, parts.size()));
  }

  public MapBasedListPartitioner(Function<T, String> keyFunction, Map<String, Integer> positionMap,
                                 BiFunction<List<List<T>>, List<T>, List<List<T>>> notFoundFunction) {
    super(
      t -> {
        Object object = keyFunction.apply(t);
        String key = (object instanceof WikiPage) ? ((WikiPage) object).getFullPath().toString() : object.toString();
        Integer value = positionMap.get(key);
        return Optional.ofNullable(value);
      },
      notFoundFunction);
    this.positionMap = positionMap;
  }

  @Override
  protected List<T> addUsingPositionFunction(List<T> source, List<List<T>> result) {
    if (positionMap.isEmpty()) {
      return source;
    } else {
      return super.addUsingPositionFunction(source, result);
    }
  }
}
