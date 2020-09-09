package fitnesse.wikitext;

import java.util.Optional;
import java.util.function.Consumer;

public interface SyntaxTree {
  String translateToHtml();

  Optional<String> findVariable(String name);
  void findPaths(Consumer<String> takePath);
  void findXrefs(Consumer<String> takeXref);
}
