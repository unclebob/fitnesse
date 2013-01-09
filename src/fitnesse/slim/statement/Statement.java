package fitnesse.slim.statement;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimError;
import fitnesse.slim.StatementExecutorInterface;

import java.util.List;

import static java.lang.String.format;

public interface Statement {
  Object execute(StatementExecutorInterface executor);
}
