package fitnesse.wikitext.parser;


import fitnesse.wikitext.shared.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

class Translate implements Translation{
  public static Translate with(BiFunction<String[], PropertySource, String> method) {
    return new Translate(method);
  }

  public static Translate with(Function<String[], String> method) {
    return new Translate((strings, source) -> method.apply(strings));
  }

  public static Translate with(Supplier<String> method) {
    return new Translate((strings, source) -> method.get());
  }

  Translate(BiFunction<String[], PropertySource, String> method) {
    this.method = method;
  }

  Translate child(int index) {
    arguments.add((translator, symbol) -> translator.translate(symbol.childAt(index)));
    return this;
  }

  Translate content() {
    arguments.add((translator, symbol) -> symbol.getContent());
    return this;
  }

  Translate text(String content) {
    arguments.add((t,s) -> content);
    return this;
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    return method.apply(arguments.stream().map(a -> a.toTarget(translator, symbol)).toArray(String[]::new), symbol);
  }

  private final BiFunction<String[], PropertySource, String> method;
  private final List<Translation> arguments = new ArrayList<>();
}
