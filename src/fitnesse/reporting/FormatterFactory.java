package fitnesse.reporting;

import java.util.ArrayList;
import java.util.List;

import fitnesse.components.ComponentFactory;

public class FormatterFactory implements FormatterRegistry {


  private final ComponentFactory componentFactory;
  private List<Class<? extends Formatter>> formatters;

  public FormatterFactory(ComponentFactory componentFactory) {
    this.componentFactory = componentFactory;
    formatters = new ArrayList<>();
  }

  @Override
  public void registerFormatter(Class<? extends Formatter> formatter) {
    formatters.add(formatter);
  }

  public Formatter[] createFormatters() {
    Formatter[] instances = new Formatter[formatters.size()];
    for (int i = 0; i < formatters.size(); i++) {
      instances[i] = componentFactory.createComponent(formatters.get(i));
    }
    return instances;
  }
}
