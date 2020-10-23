package fitnesse.wikitext.parser;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class Translate implements Translation{
  public static Translate with(Function<String[], String> method) {
    return new Translate(method);
  }

  Translate(Function<String[], String> method) {
    this.method = method;
  }

  Translate child(int index) {
    arguments.add((translator, symbol) -> translator.translate(symbol.childAt(index)));
    return this;
  }

  Translate text(String content) {
    arguments.add((s,t) -> content);
    return this;
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    return method.apply(arguments.stream().map(a -> a.toTarget(translator, symbol)).toArray(String[]::new));
  }

  private final Function<String[], String> method;
  private final List<Translation> arguments = new ArrayList<>();
}
