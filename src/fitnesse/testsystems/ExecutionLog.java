package fitnesse.testsystems;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: arjan
 * Date: 6/29/13
 * Time: 9:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ExecutionLog {
  void addException(Throwable e);

  List<Throwable> getExceptions();

  String getCommand();

  long getExecutionTime();

  int getExitCode();

  String getCapturedOutput();

  String getCapturedError();

  boolean hasCapturedOutput();
}
