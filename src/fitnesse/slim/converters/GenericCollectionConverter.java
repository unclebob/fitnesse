package fitnesse.slim.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import fitnesse.slim.Converter;
import fitnesse.util.StringUtils;

public class GenericCollectionConverter<T, C extends Collection<T>> implements Converter<C> {

  public static final Map<Class<?>, Class<?>> DEFAULT_COLLECTION_IMPL = new HashMap<>();
  static {
    DEFAULT_COLLECTION_IMPL.put(List.class, ArrayList.class);
    DEFAULT_COLLECTION_IMPL.put(Set.class, HashSet.class);
    DEFAULT_COLLECTION_IMPL.put(Queue.class, PriorityQueue.class);
    DEFAULT_COLLECTION_IMPL.put(Collection.class, ArrayList.class);
  }

  private final Class<C> collectionClass;
  private final Converter<T> componentConverter;

  @SuppressWarnings("unchecked")
  public GenericCollectionConverter(Class<?> collectionClass, Converter<T> componentConverter) {
    if (collectionClass.isInterface())
      collectionClass = (Class<C>) DEFAULT_COLLECTION_IMPL.get(collectionClass);

    this.collectionClass = (Class<C>) collectionClass;
    this.componentConverter = componentConverter;
  }

  @Override
  public String toString(C collection) {
    if (collection == null)
      return NULL_VALUE;

    int size = collection.size();
    List<String> ret = new ArrayList<>(size);
    for (T item : collection) {
      ret.add(getElementString(item));
    }
    return ListConverterHelper.toString(ret);
  }

  private String getElementString(T item) {
    String result;
    if (item == null) {
      result = componentConverter.toString(item);
    } else {
      result = ElementConverterHelper.elementToString(item);
    }
    return result;
  }

  @Override
  public C fromString(String arg) {
    if (StringUtils.isBlank(arg))
      return null;

    C collection;
    try {
      collection = collectionClass.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Collection should have a default constructor", e);
    }
    String[] strings = ListConverterHelper.fromStringToArrayOfStrings(arg);
    for (String string : strings) {
      collection.add(componentConverter.fromString(string));
    }
    return collection;
  }

}
