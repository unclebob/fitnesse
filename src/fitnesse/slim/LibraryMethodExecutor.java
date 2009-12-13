package fitnesse.slim;

import java.util.List;

public class LibraryMethodExecutor extends MethodExecutor {

  private final List<Library> libraries;

  public LibraryMethodExecutor(List<Library> libraries) {
    this.libraries = libraries;
  }

  @Override
  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args)
      throws Throwable {
    for (int i = (libraries.size() - 1); i >= 0; i--) {
      MethodExecutionResult result = findAndInvoke(methodName, args, libraries.get(i).instance);
      if (result.hasResult()) {
        return result;
      }
    }
    return MethodExecutionResult.NO_METHOD_IN_LIBRARIES;
  }

}
