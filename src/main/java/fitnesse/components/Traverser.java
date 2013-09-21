package fitnesse.components;

public interface Traverser<T> {

  void traverse(TraversalListener<T> traversalListener);

}
