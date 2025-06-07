package fitnesse.slim.converters;

import fitnesse.slim.Converter;

import java.util.*;

public class GenericCollectionConverter<T, C extends Collection<T>> extends ConverterBase<C> {

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
      collectionClass = DEFAULT_COLLECTION_IMPL.get(collectionClass);

    this.collectionClass = (Class<C>) collectionClass;
    this.componentConverter = componentConverter;
  }

  @Override
  public String getString(C collection) {
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
  public C getObject(String arg) {
    C collection;
    try {
      collection = collectionClass.getDeclaredConstructor().newInstance();
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
